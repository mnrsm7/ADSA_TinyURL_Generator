package com.example.urlshortener.service;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.util.Base62Encoder;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    private final UrlRepository repository;

    public UrlService(UrlRepository repository) {
        this.repository = repository;
    }

    public String shortenUrl(String longUrl) {
        String shortCode = Base62Encoder.encode(longUrl.hashCode());

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(shortCode);
        mapping.setOriginalUrl(longUrl);
        mapping.setClickCount(0);

        repository.save(mapping);
        return shortCode;
    }

    public UrlMapping getOriginalUrl(String code) {
        return repository.findByShortCode(code).orElse(null);
    }

    public void incrementClick(UrlMapping mapping) {
        mapping.setClickCount(mapping.getClickCount() + 1);
        repository.save(mapping);
    }
}
