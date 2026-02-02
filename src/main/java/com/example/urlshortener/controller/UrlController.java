package com.example.urlshortener.controller;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class UrlController {

    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    // 🔹 Create short URL (POST endpoint with optional expiration)
    @PostMapping("/shorten")
    public ResponseEntity<Map<String, Object>> shortenUrlPost(@RequestBody Map<String, String> request) {
        String longUrl = request.get("longUrl");
        String expiresIn = request.get("expiresIn"); // Format: "1h", "1d", "7d"
        
        if (longUrl == null || longUrl.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "longUrl is required"));
        }
        
        LocalDateTime expiresAt = null;
        if (expiresIn != null && !expiresIn.isEmpty()) {
            expiresAt = parseExpirationTime(expiresIn);
        }
        
        String code = service.shortenUrlWithExpiration(longUrl, expiresAt);
        String shortUrl = "http://localhost:8080/r/" + code;
        
        Map<String, Object> response = new HashMap<>();
        response.put("shortUrl", shortUrl);
        response.put("shortCode", code);
        response.put("originalUrl", longUrl);
        response.put("expiresAt", expiresAt);
        response.put("createdAt", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    // 🔹 Create short URL (GET endpoint - legacy support)
    @GetMapping("/shorten")
    public String shorten(@RequestParam String url) {
        String code = service.shortenUrl(url);
        return "http://localhost:8080/r/" + code;
    }

    // 🔹 Redirect short URL to original URL
    @GetMapping("/r/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code,
                                         @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent,
                                         @RequestHeader(value = "Referer", defaultValue = "") String referer,
                                         @RequestParam(value = "ip", defaultValue = "") String clientIp) {

        UrlMapping mapping = service.getOriginalUrl(code);

        if (mapping == null) {
            return ResponseEntity.notFound().build();
        }

        service.incrementClick(mapping, userAgent, clientIp, referer);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(mapping.getOriginalUrl()))
                .build();
    }

    // 🔹 Get analytics for a specific URL
    @GetMapping("/analytics/{code}")
    public ResponseEntity<Map<String, Object>> getUrlAnalytics(@PathVariable String code) {
        Map<String, Object> analytics = service.getUrlAnalytics(code);
        
        if (analytics == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(analytics);
    }

    // 🔹 Get top 10 most clicked URLs
    @GetMapping("/analytics/top/urls")
    public ResponseEntity<Map<String, Object>> getTopUrls() {
        List<UrlMapping> topUrls = service.getTopUrls();
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", topUrls.size());
        response.put("urls", topUrls);
        
        return ResponseEntity.ok(response);
    }

    // 🔹 Get all active URLs
    @GetMapping("/analytics/active/urls")
    public ResponseEntity<Map<String, Object>> getAllActiveUrls() {
        List<UrlMapping> activeUrls = service.getAllActiveUrls();
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", activeUrls.size());
        response.put("urls", activeUrls);
        
        return ResponseEntity.ok(response);
    }

    // 🔹 Delete expired URLs
    @DeleteMapping("/admin/cleanup-expired")
    public ResponseEntity<Map<String, String>> cleanupExpiredUrls() {
        service.deleteExpiredUrls();
        return ResponseEntity.ok(Map.of("message", "Expired URLs cleaned up successfully"));
    }

    // 🔹 Get URL history from stack
    @GetMapping("/analytics/history")
    public ResponseEntity<Map<String, Object>> getUrlHistory() {
        Map<String, Object> history = service.getUrlHistory();
        return ResponseEntity.ok(history);
    }

    // 🔹 Get most recent URL from history
    @GetMapping("/analytics/history/recent")
    public ResponseEntity<Object> getMostRecentHistory() {
        var recent = service.getMostRecentHistory();
        
        if (recent == null) {
            return ResponseEntity.ok(Map.of("message", "No history available"));
        }
        
        return ResponseEntity.ok(recent);
    }

    /**
     * Helper method to parse expiration time
     * Formats: "1h", "24h", "7d", "30d"
     */
    private LocalDateTime parseExpirationTime(String expiresIn) {
        LocalDateTime now = LocalDateTime.now();
        
        if (expiresIn.endsWith("h")) {
            int hours = Integer.parseInt(expiresIn.replace("h", ""));
            return now.plusHours(hours);
        } else if (expiresIn.endsWith("d")) {
            int days = Integer.parseInt(expiresIn.replace("d", ""));
            return now.plusDays(days);
        } else if (expiresIn.endsWith("m")) {
            int minutes = Integer.parseInt(expiresIn.replace("m", ""));
            return now.plusMinutes(minutes);
        }
        
        return null;
    }
}
