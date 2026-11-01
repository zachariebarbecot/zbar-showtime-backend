package fr.zbar.showtime.backend.core.search.application;

import java.util.List;

public record SearchResultPage(
    int page, int totalPages, long totalResults, List<SearchResultItem> results) {}
