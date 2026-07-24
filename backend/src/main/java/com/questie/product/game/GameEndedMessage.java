package com.questie.product.game;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class GameEndedMessage {
    private String type;
    private Map<String, Integer> scores = new HashMap<>();

    public GameEndedMessage (GameSession gameSession) {
        this.type = "GAME_ENDED";
        this.scores = gameSession.getScores();
    }
}
