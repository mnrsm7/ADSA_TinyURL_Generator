package com.example.urlshortener.controller;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class UrlController {

    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    // 🔹 Create short URL (POST endpoint for JSON requests)
    @PostMapping("/shorten")
    public ResponseEntity<Map<String, String>> shortenUrlPost(@RequestBody Map<String, String> request) {
        String longUrl = request.get("longUrl");
        
        if (longUrl == null || longUrl.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "longUrl is required"));
        }
        
        String code = service.shortenUrl(longUrl);
        String shortUrl = "http://localhost:8080/r/" + code;
        
        Map<String, String> response = new HashMap<>();
        response.put("shortUrl", shortUrl);
        response.put("shortCode", code);
        
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
    public ResponseEntity<Void> redirect(@PathVariable String code) {

        UrlMapping mapping = service.getOriginalUrl(code);

        if (mapping == null) {
            return ResponseEntity.notFound().build();
        }

        service.incrementClick(mapping);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(mapping.getOriginalUrl()))
                .build();
    }
}
