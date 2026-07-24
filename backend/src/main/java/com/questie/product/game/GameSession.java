package com.questie.product.game;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class GameSession {
    private String roomCode;
    private String currentWord;
    private int currentRound;
    private String roundWinnerId;
    private Map<String, Integer> scores = new HashMap<>();

    public GameSession (String roomCode) {
        this.currentRound = 1;
        this.roomCode = roomCode;
    }

}
