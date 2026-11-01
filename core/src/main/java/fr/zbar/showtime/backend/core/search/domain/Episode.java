package fr.zbar.showtime.backend.core.search.domain;

import java.time.LocalDate;

public record Episode(
    int episodeNumber,
    String name,
    String overview,
    Integer runtime,
    LocalDate airDate,
    String stillUrl) {}
