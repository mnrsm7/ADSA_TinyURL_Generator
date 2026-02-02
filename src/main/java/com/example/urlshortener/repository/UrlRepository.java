package com.example.urlshortener.repository;

import com.example.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortCode(String shortCode);

    @Query("SELECT u FROM UrlMapping u WHERE u.expiresAt IS NULL OR u.expiresAt > CURRENT_TIMESTAMP ORDER BY u.clickCount DESC LIMIT 10")
    List<UrlMapping> findTop10ActiveUrls();

    @Query("SELECT u FROM UrlMapping u WHERE u.expiresAt IS NULL OR u.expiresAt > CURRENT_TIMESTAMP ORDER BY u.createdAt DESC")
    List<UrlMapping> findAllActiveUrls();

    @Query("SELECT u FROM UrlMapping u WHERE u.expiresAt IS NOT NULL AND u.expiresAt <= CURRENT_TIMESTAMP")
    List<UrlMapping> findExpiredUrls();

    @Query("SELECT u FROM UrlMapping u WHERE u.createdAt BETWEEN :startDate AND :endDate ORDER BY u.createdAt DESC")
    List<UrlMapping> findUrlsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
