package com.example.urlshortener.repository;

import com.example.urlshortener.model.UrlAccessLog;
import com.example.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UrlAccessLogRepository extends JpaRepository<UrlAccessLog, Long> {
    
    List<UrlAccessLog> findByUrlMapping(UrlMapping urlMapping);

    @Query("SELECT u FROM UrlAccessLog u WHERE u.urlMapping = :urlMapping ORDER BY u.accessedAt DESC")
    List<UrlAccessLog> findAccessLogsByUrl(@Param("urlMapping") UrlMapping urlMapping);

    @Query("SELECT u FROM UrlAccessLog u WHERE u.accessedAt BETWEEN :startDate AND :endDate ORDER BY u.accessedAt DESC")
    List<UrlAccessLog> findAccessLogsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(u) FROM UrlAccessLog u WHERE u.urlMapping = :urlMapping")
    long countAccessesByUrl(@Param("urlMapping") UrlMapping urlMapping);
}
