package com.questie.product.service;

import com.questie.product.entity.Player;
import com.questie.product.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public Player createGuest() {
        String guestNumber = String.valueOf((int)(Math.random() * 9000) + 1000);

        Player player = Player.builder()
                .username("Guest#" + guestNumber)
                .guest(true)
                .build();

        return playerRepository.save(player);
    }
}