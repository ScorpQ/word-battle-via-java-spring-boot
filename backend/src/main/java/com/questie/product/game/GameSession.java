package com.questie.product.game;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSession {

    @Getter
    private final String roomCode;

    /**
     * Oyunun tum turlari, sirasiyla. Oyun basinda tek sorguda cekiliyor ve
     * ayni kelime iki kez giremiyor; tekrar sorunu bu yuzden hic olusmuyor.
     */
    private final List<String> words;

    private final Map<String, Integer> scores = new HashMap<>();

    private int currentRound;
    private String roundWinnerId;

    /** Tur kapandi mi: ya biri bildi ya da sure doldu. Ikisi de ayni turu iki kez kapatamaz. */
    private boolean roundOver;
    private long roundStartedAt;

    public GameSession(String roomCode, List<String> words) {
        this.roomCode = roomCode;
        this.words = List.copyOf(words);
        this.currentRound = 1;
        this.roundStartedAt = System.currentTimeMillis();
    }

    /** Tur sayisi kelime bankasindan cekilebilen kadar; normalde istenen tur sayisi. */
    public int getTotalRounds() {
        return words.size();
    }

    public synchronized int getCurrentRound() {
        return currentRound;
    }

    public synchronized String getCurrentWord() {
        return currentRound <= words.size() ? words.get(currentRound - 1) : null;
    }

    public synchronized String getRoundWinnerId() {
        return roundWinnerId;
    }

    public synchronized Map<String, Integer> getScores() {
        return scores;
    }

    public synchronized boolean isLastRound() {
        return currentRound >= words.size();
    }

    /**
     * Turu kazanmayi dener. Kelime kontrolu ile tur kapatma tek kilit altinda
     * yapildigi icin, ayni anda gelen iki dogru cevaptan yalnizca biri true alir.
     * Ikinci cevap turu tekrar ilerletemez.
     *
     * Karsilastirma buyuk/kucuk harf duyarsiz ve bosluklar kirpilarak yapilir;
     * kelimeler veritabaninda buyuk harfle duruyor.
     */
    public synchronized boolean claimWin(String playerId, String answer) {
        String word = getCurrentWord();

        if (roundOver || word == null || answer == null) {
            return false;
        }
        if (!word.equalsIgnoreCase(answer.trim())) {
            return false;
        }

        roundOver = true;
        roundWinnerId = playerId;
        return true;
    }

    /**
     * Tur suresi dolduysa turu kazanansiz kapatir. claimWin ile ayni kilidi
     * paylastigi icin, son anda gelen dogru cevapla zamanasimi carpismaz:
     * hangisi kilide once girerse tur onun sekliyle kapanir.
     *
     * @return yalnizca turu bu cagri kapattiysa true
     */
    public synchronized boolean expireRound(long timeoutMillis) {
        if (roundOver || getCurrentWord() == null) {
            return false;
        }
        if (System.currentTimeMillis() - roundStartedAt < timeoutMillis) {
            return false;
        }

        roundOver = true;
        return true;
    }

    public synchronized void addPoints(String playerId, int points) {
        scores.merge(playerId, points, Integer::sum);
    }

    /** Sonraki tura gecer: turu artirir, tur durumunu ve sureyi sifirlar. */
    public synchronized void advance() {
        currentRound++;
        roundWinnerId = null;
        roundOver = false;
        roundStartedAt = System.currentTimeMillis();
    }
}
