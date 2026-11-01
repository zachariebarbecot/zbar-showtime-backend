package fr.zbar.showtime.backend.core.search.application;

import java.util.UUID;

public record SearchTitlesCommand(UUID userId, String query, int page, String type) {}
