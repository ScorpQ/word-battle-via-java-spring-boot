package com.questie.product.repository;

import com.questie.product.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, String> {
    List<Score> findByRoomIdOrderByPointsDesc(String roomId);
    List<Score> findByPlayerIdOrderByCreatedAtDesc(String playerId);
}