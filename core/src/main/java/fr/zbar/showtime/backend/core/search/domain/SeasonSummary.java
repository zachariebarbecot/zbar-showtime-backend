package fr.zbar.showtime.backend.core.search.domain;

import java.time.LocalDate;

public record SeasonSummary(
    int seasonNumber, String name, int episodeCount, LocalDate airDate, String posterUrl) {}
