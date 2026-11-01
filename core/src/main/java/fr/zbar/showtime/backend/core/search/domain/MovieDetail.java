package fr.zbar.showtime.backend.core.search.domain;

import java.time.LocalDate;
import java.util.List;

public record MovieDetail(
    long id,
    String title,
    String originalTitle,
    List<String> genres,
    LocalDate releaseDate,
    Integer runtime,
    MovieStatus status,
    String overview,
    String posterUrl,
    List<String> directors,
    List<CrewMember> crew,
    List<CastMember> cast) {}
