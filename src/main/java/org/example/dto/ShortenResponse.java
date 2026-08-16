package org.example.dto;

public class ShortenResponse {

    private String shortUrl;
    private String shortCode;
    private String longUrl;

    public ShortenResponse(String shortUrl, String shortCode, String longUrl) {
        this.shortUrl = shortUrl;
        this.shortCode = shortCode;
        this.longUrl = longUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getLongUrl() {
        return longUrl;
    }
}
