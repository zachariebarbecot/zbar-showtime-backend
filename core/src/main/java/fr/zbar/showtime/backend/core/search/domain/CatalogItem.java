package fr.zbar.showtime.backend.core.search.domain;

import java.time.LocalDate;

public record CatalogItem(
    long id, MediaType type, String title, String posterUrl, LocalDate releaseDate) {}
