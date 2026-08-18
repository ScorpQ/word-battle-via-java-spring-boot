package com.questie.product.repository;

import com.questie.product.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends JpaRepository<Word, String> {

    @Query(value = "SELECT * FROM words ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Word findRandomWord();

    /**
     * Bir oyunun tum turlarini tek seferde secer. DISTINCT'e gerek yok:
     * tablodan farkli satirlar donduzu icin ayni kelime iki kez gelemez.
     * Kelime sayisi limitten azsa daha kisa liste doner.
     */
    @Query(value = "SELECT * FROM words ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Word> findRandomWords(@Param("limit") int limit);

    @Query(value = "SELECT * FROM words WHERE category = :category ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Word findRandomWordByCategory(@Param("category") String category);

    @Query(value = "SELECT * FROM words WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Word findRandomWordByDifficulty(@Param("difficulty") String difficulty);
}