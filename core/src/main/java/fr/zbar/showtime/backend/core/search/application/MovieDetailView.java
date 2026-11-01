package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.MovieDetail;

public record MovieDetailView(MovieDetail movie, String watchlistStatus) {}
