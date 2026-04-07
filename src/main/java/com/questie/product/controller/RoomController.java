package com.questie.product.controller;

import com.questie.product.dto.PlayerResponse;
import com.questie.product.dto.RoomResponse;
import com.questie.product.entity.Room;
import com.questie.product.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/create/{playerId}")
    public ResponseEntity<RoomResponse> createRoom(@PathVariable String playerId) {
        Room room = roomService.createRoom(playerId);
        return ResponseEntity.ok(toResponse(room));
    }

    @PostMapping("/join/{roomCode}/{playerId}")
    public ResponseEntity<RoomResponse> joinRoom(
            @PathVariable String roomCode,
            @PathVariable String playerId) {
        Room room = roomService.joinRoom(roomCode, playerId);
        return ResponseEntity.ok(toResponse(room));
    }

    private RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .code(room.getCode())
                .status(room.getStatus())
                .maxPlayers(room.getMaxPlayers())
                .currentRound(room.getCurrentRound())
                .totalRounds(room.getTotalRounds())
                .host(toPlayerResponse(room.getHost()))
                .players(room.getPlayers().stream()
                        .map(this::toPlayerResponse)
                        .toList())
                .createdAt(room.getCreatedAt())
                .build();
    }

    private PlayerResponse toPlayerResponse(com.questie.product.entity.Player player) {
        return PlayerResponse.builder()
                .id(player.getId())
                .username(player.getUsername())
                .email(player.getEmail())
                .guest(player.isGuest())
                .createdAt(player.getCreatedAt())
                .build();
    }
}