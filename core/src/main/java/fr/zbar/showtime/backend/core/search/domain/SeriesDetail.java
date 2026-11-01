package fr.zbar.showtime.backend.core.search.domain;

import java.time.LocalDate;
import java.util.List;

public record SeriesDetail(
    long id,
    String name,
    String originalName,
    List<String> genres,
    LocalDate firstAirDate,
    LocalDate lastAirDate,
    SeriesStatus status,
    String overview,
    String posterUrl,
    Integer numberOfSeasons,
    Integer numberOfEpisodes,
    List<String> creators,
    List<CrewMember> crew,
    List<CastMember> cast,
    List<SeasonSummary> seasons) {}
