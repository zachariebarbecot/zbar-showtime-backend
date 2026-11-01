package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;

public record SeriesDetailView(SeriesDetail series, String watchlistStatus) {}
