package com.example.urlshortener.service;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.model.UrlAccessLog;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.repository.UrlAccessLogRepository;
import com.example.urlshortener.util.Base62Encoder;
import com.example.urlshortener.util.URLHistoryStack;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class UrlService {

    private final UrlRepository repository;
    private final UrlAccessLogRepository accessLogRepository;
    private final URLHistoryStack historyStack;

    public UrlService(UrlRepository repository, UrlAccessLogRepository accessLogRepository) {
        this.repository = repository;
        this.accessLogRepository = accessLogRepository;
        this.historyStack = new URLHistoryStack();
    }

    /**
     * Shorten a URL with optional expiration
     */
    public String shortenUrl(String longUrl) {
        return shortenUrlWithExpiration(longUrl, null);
    }

    /**
     * Shorten a URL with expiration date
     */
    public String shortenUrlWithExpiration(String longUrl, LocalDateTime expiresAt) {
        // Use SHA-256, truncate to first 6 bytes (48 bits) and encode to Base62
        String shortCode = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Try a few attempts to avoid collisions by appending attempt counter
            for (int attempt = 0; attempt < 8; attempt++) {
                byte[] digest = md.digest((longUrl + (attempt == 0 ? "" : ":" + attempt)).getBytes());
                long truncated = 0L;
                // take first 6 bytes -> 48 bits
                for (int i = 0; i < 6; i++) {
                    truncated = (truncated << 8) | (digest[i] & 0xFFL);
                }
                // ensure positive
                truncated = truncated & 0xFFFFFFFFFFFFL;

                String candidate = Base62Encoder.encode(truncated);
                var existing = repository.findByShortCode(candidate);
                if (existing.isEmpty()) {
                    shortCode = candidate;
                    break;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            // fallback to original hashCode if SHA-256 unavailable
            shortCode = Base62Encoder.encode(longUrl.hashCode());
        }

        if (shortCode == null) {
            // last resort: use hashCode
            shortCode = Base62Encoder.encode(longUrl.hashCode());
        }

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(shortCode);
        mapping.setOriginalUrl(longUrl);
        mapping.setClickCount(0);
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setExpiresAt(expiresAt);

        repository.save(mapping);
        
        // Add to history stack
        historyStack.pushHistory(longUrl, shortCode);
        
        return shortCode;
    }

    /**
     * Get original URL and check expiration
     */
    public UrlMapping getOriginalUrl(String code) {
        var mapping = repository.findByShortCode(code).orElse(null);
        
        if (mapping != null && mapping.isExpired()) {
            return null; // Return null for expired URLs
        }
        
        return mapping;
    }

    /**
     * Increment click count and log access
     */
    public void incrementClick(UrlMapping mapping, String userAgent, String ipAddress, String referer) {
        mapping.setClickCount(mapping.getClickCount() + 1);
        mapping.setLastAccessedAt(LocalDateTime.now());
        repository.save(mapping);
        
        // Log access details for analytics
        UrlAccessLog accessLog = new UrlAccessLog(mapping, userAgent, ipAddress, referer);
        accessLogRepository.save(accessLog);
    }

    /**
     * Get analytics for a specific short URL
     */
    public Map<String, Object> getUrlAnalytics(String shortCode) {
        var mapping = repository.findByShortCode(shortCode).orElse(null);
        
        if (mapping == null) {
            return null;
        }

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("shortCode", shortCode);
        analytics.put("originalUrl", mapping.getOriginalUrl());
        analytics.put("totalClicks", mapping.getClickCount());
        analytics.put("createdAt", mapping.getCreatedAt());
        analytics.put("lastAccessedAt", mapping.getLastAccessedAt());
        analytics.put("expiresAt", mapping.getExpiresAt());
        analytics.put("isExpired", mapping.isExpired());
        
        // Get detailed access logs
        List<UrlAccessLog> accessLogs = accessLogRepository.findAccessLogsByUrl(mapping);
        analytics.put("accessLogs", accessLogs);
        analytics.put("totalAccessRecords", accessLogs.size());
        
        return analytics;
    }

    /**
     * Get top 10 most clicked URLs
     */
    public List<UrlMapping> getTopUrls() {
        return repository.findTop10ActiveUrls();
    }

    /**
     * Get all active (non-expired) URLs
     */
    public List<UrlMapping> getAllActiveUrls() {
        return repository.findAllActiveUrls();
    }

    /**
     * Clean up expired URLs
     */
    public void deleteExpiredUrls() {
        List<UrlMapping> expiredUrls = repository.findExpiredUrls();
        repository.deleteAll(expiredUrls);
    }

    /**
     * Get URL history from stack
     */
    public Map<String, Object> getUrlHistory() {
        Map<String, Object> history = new HashMap<>();
        history.put("historySize", historyStack.getHistorySize());
        history.put("maxSize", historyStack.getMaxSize());
        history.put("history", historyStack.getHistoryList());
        return history;
    }

    /**
     * Get the most recent URL from history (peek)
     */
    public URLHistoryStack.UrlHistoryEntry getMostRecentHistory() {
        return historyStack.peekHistory();
    }
}
