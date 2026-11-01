package fr.zbar.showtime.backend.app.search.tmdb;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.tmdb")
public record TmdbProperties(
    String baseUrl, String token, String imageBaseUrl, String posterSize, String language) {}
