package com.questie.product.service;

import com.questie.product.entity.Player;
import com.questie.product.entity.Room;
import com.questie.product.repository.PlayerRepository;
import com.questie.product.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Room createRoom(String playerId) {
        Player host = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Oyuncu bulunamadı"));

        List<Player> players = new ArrayList<>();
        players.add(host);

        Room room = Room.builder()
                .code(generateUniqueCode())
                .status(Room.RoomStatus.WAITING)
                .maxPlayers(4)
                .currentRound(0)
                .totalRounds(10)
                .players(players)
                .host(host)
                .build();

        return roomRepository.save(room);
    }

    public Room joinRoom(String roomCode, String playerId) {
        Room room = roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı"));

        if (room.getStatus() != Room.RoomStatus.WAITING) {
            throw new RuntimeException("Oyun zaten başladı");
        }

        if (room.getPlayers().size() >= room.getMaxPlayers()) {
            throw new RuntimeException("Oda dolu");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Oyuncu bulunamadı"));

        room.getPlayers().add(player);

        Room savedRoom = roomRepository.save(room);

            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomCode,
                    playerId + " oyuna katıldı"
            );

        return savedRoom;
    }

    private String generateUniqueCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;

        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (roomRepository.existsByCode(code));

        return code;
    }
}
