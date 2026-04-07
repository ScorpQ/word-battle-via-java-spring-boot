package com.questie.product.dto;

import com.questie.product.entity.Room;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RoomResponse {
    private String id;
    private String code;
    private Room.RoomStatus status;
    private int maxPlayers;
    private int currentRound;
    private int totalRounds;
    private List<PlayerResponse> players;
    private PlayerResponse host;
    private LocalDateTime createdAt;
}