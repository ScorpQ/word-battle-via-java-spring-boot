package com.questie.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    @Column(nullable = false)
    private int maxPlayers = 4;

    @Column(nullable = false)
    private int currentRound = 0;

    @Column(nullable = false)
    private int totalRounds = 10;

    @ManyToMany
    @JoinTable(
            name = "room_players",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private List<Player> players = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "host_player_id")
    private Player host;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum RoomStatus {
        WAITING,    // oyuncular bekleniyor
        IN_GAME,    // oyun aktif
        FINISHED    // oyun bitti
    }
}
