package com.questie.product.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlayerResponse {
    private  String id;
    private String username;
    private String email;
    private boolean guest;
    private LocalDateTime createdAt;
}