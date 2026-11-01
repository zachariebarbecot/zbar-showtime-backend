package fr.zbar.showtime.backend.core.watchlist.application;

import java.util.UUID;

public record RemoveSeriesFromWatchlistCommand(UUID userId, long tmdbId) {}
