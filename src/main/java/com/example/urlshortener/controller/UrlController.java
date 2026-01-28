package com.example.urlshortener.controller;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class UrlController {

    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    // 🔹 Create short URL
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
