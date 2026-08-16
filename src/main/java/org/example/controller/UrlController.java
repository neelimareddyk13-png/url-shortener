package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.dto.ShortenRequest;
import org.example.dto.ShortenResponse;
import org.example.dto.UrlStatsResponse;
import org.example.exception.RateLimitExceededException;
import org.example.model.Url;
import org.example.service.RateLimiter;
import org.example.service.UrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class UrlController {

    private final UrlService urlService;
    private final RateLimiter rateLimiter;

    @Value("${app.base-url}")
    private String baseUrl;

    public UrlController(UrlService urlService, RateLimiter rateLimiter) {
        this.urlService = urlService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request,
                                                     HttpServletRequest httpRequest) {
        if (!rateLimiter.allow(httpRequest.getRemoteAddr())) {
            throw new RateLimitExceededException();
        }
        Url url = urlService.shorten(request.getLongUrl());
        String shortUrl = baseUrl + "/" + url.getShortCode();
        ShortenResponse response = new ShortenResponse(shortUrl, url.getShortCode(), url.getLongUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        Url url = urlService.resolveAndRecordClick(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url.getLongUrl()))
                .build();
    }

    @GetMapping("/api/urls/{shortCode}/stats")
    public ResponseEntity<UrlStatsResponse> stats(@PathVariable String shortCode) {
        Url url = urlService.getByShortCode(shortCode);
        UrlStatsResponse response = new UrlStatsResponse(
                url.getShortCode(),
                url.getLongUrl(),
                url.getClickCount(),
                url.getCreatedAt(),
                url.getLastAccessedAt()
        );
        return ResponseEntity.ok(response);
    }
}
