package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.watchlist.domain.MovieStatus;
import java.util.UUID;

public record ChangeMovieStatusCommand(UUID userId, long tmdbId, MovieStatus status) {}
