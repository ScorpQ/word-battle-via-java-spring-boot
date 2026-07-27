package com.questie.product.service;

import com.questie.product.entity.Room;
import com.questie.product.entity.Word;
import com.questie.product.game.GameEndedMessage;
import com.questie.product.game.GameSession;
import com.questie.product.repository.RoomRepository;
import com.questie.product.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GameService {
    private final RoomRepository roomRepository;
    private final WordRepository wordRepository;
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

        room.setStatus(Room.RoomStatus.IN_GAME);
        room.setCurrentRound(1);
        roomRepository.save(room);

        GameSession gameSession = new GameSession(roomCode);
        activeSessions.put(roomCode, gameSession);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                "GAME_STARTED"
        );

        startRound(room, gameSession);
    }

    private void startRound(Room room, GameSession gameSession) {
        Word word = wordRepository.findRandomWord();

        gameSession.setCurrentWord(word.getValue());

        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getCode(),
                "ROUND_STARTED:" + room.getCurrentRound() + ":" + word.getValue()
        );
    }

    public void checkAnswer(String roomCode, String playerId, String answer) {

        // todo convertAndSend sırasında gönderilen obje daha güvenli hale gelmeli.
        GameSession gameSession = activeSessions.get(roomCode);

        // todo roundWinnerId kontrolü eklenecek ki doğru cevaptan sonra doğru cevap gelmeye devam etmesin.
        if (gameSession.getCurrentWord().equals(answer)) {
            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomCode,
                    "DOĞRU CEVAP: " + playerId);

            nextRound(gameSession, playerId);
        } else {
            // todo convertAndSend sırasında gönderilen obje daha güvenli hale gelmeli.
            // todo header'a sessionId eklemek icin bu kar koda ihtiyaç var mıydı öğren.

            SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setSessionId(playerSessions.get(playerId));
            headerAccessor.setLeaveMutable(true);

            messagingTemplate.convertAndSendToUser(
                    playerSessions.get(playerId),
                    "/queue/answer/" + roomCode,
                    gameSession,
                    headerAccessor.getMessageHeaders());
        }
    }

    public  GameSession nextRound(GameSession gameSession, String playerId) {
        // Puan once verilmeli: son turda asagida endTheSession'a girip return
        // ettigimiz icin, buraya konmazsa 10. turun puani hic eklenmiyordu.
        int currentScore = gameSession.getScores().getOrDefault(playerId, 0);
        gameSession.getScores().put(playerId, currentScore + 10);

        if (gameSession.getCurrentRound() == 10) {
            endTheSession(gameSession);
            return gameSession;
        } else {
            Word word = wordRepository.findRandomWord();
            gameSession.setCurrentWord(word.getValue());

            gameSession.setCurrentRound(gameSession.getCurrentRound() + 1);
            gameSession.setRoundWinnerId(null);

            messagingTemplate.convertAndSend(
                    "/topic/room/" + gameSession.getRoomCode(),
                    "ROUND_STARTED:" + gameSession.getCurrentRound() + ":" + word.getValue()
            );
        }

        return gameSession;
    }

    public void endTheSession(GameSession gameSession) {
        GameEndedMessage gameEndedMessage = new GameEndedMessage(gameSession);
        activeSessions.remove(gameSession.getRoomCode());

        Room room = roomRepository.findByCode(gameSession.getRoomCode())
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı"));
        room.setStatus(Room.RoomStatus.FINISHED);
        roomRepository.save(room);

        messagingTemplate.convertAndSend(
                "/topic/room/" + gameSession.getRoomCode(),
                gameEndedMessage);
    }
}
