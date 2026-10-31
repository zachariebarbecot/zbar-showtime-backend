package fr.zbar.showtime.backend.core.domain;

import java.util.List;

public record SearchPage(int page, int totalPages, long totalResults, List<CatalogItem> results) {}
