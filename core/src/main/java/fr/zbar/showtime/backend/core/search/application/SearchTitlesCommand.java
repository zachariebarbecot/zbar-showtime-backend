package fr.zbar.showtime.backend.core.search.application;

public record SearchTitlesCommand(String query, int page, String type) {}
