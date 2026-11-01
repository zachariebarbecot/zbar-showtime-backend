package fr.zbar.showtime.backend.core.search.domain;

import java.time.LocalDate;
import java.util.List;

public record SeasonDetail(
    int seasonNumber,
    String name,
    String overview,
    LocalDate airDate,
    String posterUrl,
    List<Episode> episodes) {}
