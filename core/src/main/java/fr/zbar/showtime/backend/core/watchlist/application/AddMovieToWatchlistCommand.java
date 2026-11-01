package fr.zbar.showtime.backend.core.watchlist.application;

import java.util.UUID;

public record AddMovieToWatchlistCommand(UUID userId, long tmdbId) {}
