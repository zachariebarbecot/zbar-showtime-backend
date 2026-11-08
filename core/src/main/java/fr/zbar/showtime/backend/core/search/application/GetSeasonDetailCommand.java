package fr.zbar.showtime.backend.core.search.application;

import java.util.UUID;

public record GetSeasonDetailCommand(UUID userId, long tmdbId, int seasonNumber) {}
