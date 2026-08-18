package com.questie.product.service;

import com.questie.product.entity.Player;
import com.questie.product.entity.Room;
import com.questie.product.entity.Score;
import com.questie.product.entity.Word;
import com.questie.product.game.GameEndedMessage;
import com.questie.product.game.GameSession;
import com.questie.product.repository.RoomRepository;
import com.questie.product.repository.ScoreRepository;
import com.questie.product.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GameService {
    // todo Room.totalRounds alanindan okunmali; simdilik sabit.
    private static final int TOTAL_ROUNDS = 10;
    private static final int POINTS_PER_ROUND = 10;

    /** Bir turun cevaplanmasi icin verilen sure. Dolunca tur kazanansiz kapanir. */
    private static final long ROUND_TIMEOUT_MILLIS = 20_000;

    private final RoomRepository roomRepository;
    private final WordRepository wordRepository;
    private final ScoreRepository scoreRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, String> playerSessions = new ConcurrentHashMap<>();

    public void registerSession(String playerId, String sessionId) {
        playerSessions.put(playerId, sessionId);
    }

    public void startGame(String roomCode, String playerId) {
        Room room = roomRepository.findByCodeWithPlayers(roomCode)
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı"));

        // Daha sonra frontendde hsot kontrolü ile buraya istek atacak olan butonu ui'dan kaldırman lazım.
        if (!room.getHost().getId().equals(playerId)) {
            throw new RuntimeException("Sadece host oyunu başlatabilir");
        }

        if (room.getPlayers().size() < 2) {
            throw new RuntimeException("En az 2 oyuncu gerekli");
        }

        // Oyunun tum kelimeleri tek sorguda ve tekrarsiz seciliyor.
        List<String> words = wordRepository.findRandomWords(TOTAL_ROUNDS).stream()
                .map(Word::getValue)
                .toList();

        if (words.isEmpty()) {
            throw new RuntimeException("Kelime bankası boş");
        }

        room.setStatus(Room.RoomStatus.IN_GAME);
        room.setCurrentRound(1);
        roomRepository.save(room);

        GameSession gameSession = new GameSession(roomCode, words);
        activeSessions.put(roomCode, gameSession);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                "GAME_STARTED"
        );

        broadcastRoundStarted(gameSession);
    }

    private void broadcastRoundStarted(GameSession gameSession) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + gameSession.getRoomCode(),
                "ROUND_STARTED:" + gameSession.getCurrentRound() + ":" + gameSession.getCurrentWord()
        );
    }

    public void checkAnswer(String roomCode, String playerId, String answer) {
        GameSession gameSession = activeSessions.get(roomCode);

        // Oyun bitmis veya hic baslamamis bir odaya cevap gelebilir.
        if (gameSession == null) {
            return;
        }

        // Kontrol ve kazanan isaretlemesi GameSession icinde tek kilit altinda
        // yapiliyor; ayni anda gelen iki dogru cevaptan sadece biri true alir.
        if (gameSession.claimWin(playerId, answer)) {
            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomCode,
                    "CORRECT_ANSWER:" + playerId);

            nextRound(gameSession, playerId);
        } else {
            sendToPlayer(playerId, roomCode, "WRONG_ANSWER");
        }
    }

    /**
     * Tek bir oyuncuya mesaj gonderir. Oyuncular kimlik dogrulamadan baglandigi
     * icin ortada Principal yok; bu yuzden hedef, CONNECT sirasinda kaydedilen
     * STOMP sessionId'si uzerinden belirleniyor. Header'a sessionId yazmak ve
     * leaveMutable'i acik birakmak Spring'in bu mesaji ilgili oturuma
     * yonlendirebilmesi icin gerekli.
     */
    private void sendToPlayer(String playerId, String roomCode, Object payload) {
        String sessionId = playerSessions.get(playerId);
        if (sessionId == null) {
            return;
        }

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/answer/" + roomCode,
                payload,
                headerAccessor.getMessageHeaders());
    }

    /**
     * Turu kapatip sonrakine gecer. playerId null ise tur zamanasimiyla
     * kapanmistir ve kimseye puan yazilmaz.
     */
    private void nextRound(GameSession gameSession, String playerId) {
        // Puan once verilmeli: son turda asagida endTheSession'a girip return
        // ettigimiz icin, buraya konmazsa 10. turun puani hic eklenmiyordu.
        if (playerId != null) {
            gameSession.addPoints(playerId, POINTS_PER_ROUND);
        }

        if (gameSession.isLastRound()) {
            endTheSession(gameSession);
            return;
        }

        gameSession.advance();
        broadcastRoundStarted(gameSession);
    }

    /**
     * Suresi dolan turlari kapatir. Her tur icin ayri bir zamanlanmis gorev
     * olusturup dogru cevap gelince iptal etmek yerine tek bir tarayici
     * kullaniliyor: iptal etme muhasebesi olmadigi icin yaris ihtimali de yok.
     *
     * Kapatma karari GameSession.expireRound icinde, claimWin ile ayni kilit
     * altinda veriliyor; son saniyede gelen dogru cevapla carpismaz.
     *
     * Bu ayni zamanda yarim kalan oyunlarin bellekte birikmesini engelliyor:
     * kimse cevap vermese bile oyun turlari tuketip normal sekilde bitiyor.
     */
    @Scheduled(fixedDelay = 1000)
    public void expireTimedOutRounds() {
        for (GameSession gameSession : activeSessions.values()) {
            if (!gameSession.expireRound(ROUND_TIMEOUT_MILLIS)) {
                continue;
            }

            messagingTemplate.convertAndSend(
                    "/topic/room/" + gameSession.getRoomCode(),
                    "ROUND_TIMEOUT:" + gameSession.getCurrentRound() + ":" + gameSession.getCurrentWord());

            nextRound(gameSession, null);
        }
    }

    public void endTheSession(GameSession gameSession) {
        GameEndedMessage gameEndedMessage = new GameEndedMessage(gameSession);
        activeSessions.remove(gameSession.getRoomCode());

        // Oyuncular fetch join ile geliyor: skorlari yazarken listeyi dolasacagimiz
        // icin lazy koleksiyon burada ise yaramaz.
        Room room = roomRepository.findByCodeWithPlayers(gameSession.getRoomCode())
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı"));
        room.setStatus(Room.RoomStatus.FINISHED);
        roomRepository.save(room);

        saveScores(room, gameSession);

        messagingTemplate.convertAndSend(
                "/topic/room/" + gameSession.getRoomCode(),
                gameEndedMessage);
    }

    /**
     * Oyun sonu skorlarini kalici hale getirir. Bellekteki oturum birazdan
     * silinecegi icin veriyi diske tasiyan tek nokta burasi.
     *
     * Odadaki tum oyuncular kaydedilir; hic tur kazanamayan da 0 puanla girer,
     * boylece oynanmis bir oyunun katilimci listesi eksiksiz kalir.
     */
    private void saveScores(Room room, GameSession gameSession) {
        Map<String, Integer> points = gameSession.getScores();

        List<Player> ranked = room.getPlayers().stream()
                .sorted(Comparator.comparingInt(
                        (Player player) -> points.getOrDefault(player.getId(), 0)).reversed())
                .toList();

        List<Score> rows = new ArrayList<>();
        int rank = 0;
        int previousPoints = Integer.MIN_VALUE;

        for (int i = 0; i < ranked.size(); i++) {
            Player player = ranked.get(i);
            int playerPoints = points.getOrDefault(player.getId(), 0);

            // Esit puanlilar ayni sirayi paylasir, sonraki sira atlar: 1, 2, 2, 4.
            if (playerPoints != previousPoints) {
                rank = i + 1;
                previousPoints = playerPoints;
            }

            rows.add(Score.builder()
                    .player(player)
                    .room(room)
                    .points(playerPoints)
                    .rank(rank)
                    .build());
        }

        scoreRepository.saveAll(rows);
    }
}
