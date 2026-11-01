package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus;
import java.util.UUID;

public record ChangeSeriesStatusCommand(UUID userId, long tmdbId, SeriesStatus status) {}
