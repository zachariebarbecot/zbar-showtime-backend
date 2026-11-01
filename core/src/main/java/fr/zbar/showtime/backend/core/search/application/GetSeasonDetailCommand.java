package fr.zbar.showtime.backend.core.search.application;

public record GetSeasonDetailCommand(long tmdbId, int seasonNumber) {}
