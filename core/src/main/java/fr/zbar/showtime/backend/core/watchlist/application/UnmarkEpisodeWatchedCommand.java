package fr.zbar.showtime.backend.core.watchlist.application;

import java.util.UUID;

public record UnmarkEpisodeWatchedCommand(
    UUID userId, long tmdbId, int seasonNumber, int episodeNumber) {}
