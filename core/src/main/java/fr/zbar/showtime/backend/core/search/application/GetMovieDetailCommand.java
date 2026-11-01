package fr.zbar.showtime.backend.core.search.application;

import java.util.UUID;

public record GetMovieDetailCommand(UUID userId, long tmdbId) {}
