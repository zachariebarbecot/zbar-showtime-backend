package fr.zbar.showtime.backend.core.domain;

import java.time.LocalDate;

public record CatalogItem(
    long id, MediaType type, String title, String posterUrl, LocalDate releaseDate) {}
