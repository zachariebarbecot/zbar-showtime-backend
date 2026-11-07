package fr.zbar.showtime.backend.core.watchlist.application;

import java.util.UUID;

public record MarkEpisodeWatchedCommand(
    UUID userId, long tmdbId, int seasonNumber, int episodeNumber) {}
