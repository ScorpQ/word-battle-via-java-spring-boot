package com.questie.product.repository;

import com.questie.product.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    Optional<Room> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.players WHERE r.code = :code")
    Optional<Room> findByCodeWithPlayers(@Param("code") String code);
}