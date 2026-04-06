package com.questie.product.controller;

import com.questie.product.entity.Player;
import com.questie.product.dto.PlayerResponse;
import com.questie.product.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("/guest")
    public ResponseEntity<PlayerResponse> createGuest() {
        Player player = playerService.createGuest();
        return ResponseEntity.ok(toResponse(player));
    }

    private PlayerResponse toResponse(Player player) {
        return PlayerResponse.builder()
                .id(player.getId())
                .username(player.getUsername())
                .email(player.getEmail())
                .guest(player.isGuest())
                .createdAt(player.getCreatedAt())
                .build();
    }
}