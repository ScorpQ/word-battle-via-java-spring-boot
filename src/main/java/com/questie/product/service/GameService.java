package com.questie.product.service;

import com.questie.product.entity.Room;
import com.questie.product.entity.Word;
import com.questie.product.repository.RoomRepository;
import com.questie.product.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {
    private final RoomRepository roomRepository;
    private final WordRepository wordRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void startGame(String roomCode, String playerId) {
        Room room = roomRepository.findByCode(roomCode)
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

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                "GAME_STARTED"
        );

        startRound(room);
    }

    private void startRound(Room room) {
        Word word = wordRepository.findRandomWord();

        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getCode(),
                "ROUND_STARTED:" + room.getCurrentRound() + ":" + word.getValue()
        );
    }
}