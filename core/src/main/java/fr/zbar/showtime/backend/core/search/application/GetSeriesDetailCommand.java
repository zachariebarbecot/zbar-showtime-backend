package fr.zbar.showtime.backend.core.search.application;

import java.util.UUID;

public record GetSeriesDetailCommand(UUID userId, long tmdbId) {}
