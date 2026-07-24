package com.questie.product.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartGame {
    private String playerId;
    private String answer;
}
