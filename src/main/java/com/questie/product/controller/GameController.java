package com.questie.product.controller;

import com.questie.product.dto.StartGame;
import com.questie.product.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @MessageMapping("/room/{roomCode}/start")
    public void startGame(
            @DestinationVariable String roomCode,
            @Payload StartGame payload) {
        gameService.startGame(roomCode, payload.getPlayerId());
    }

    // todo Principal kullanılarak kimlik doğrulaması yapılacak
    @MessageMapping("/room/{roomCode}/answer")
    public void submitAnswer(
            @DestinationVariable String roomCode,
            @Payload StartGame payload) {
        gameService.checkAnswer(roomCode, payload.getPlayerId(), payload.getAnswer());
    }
}
