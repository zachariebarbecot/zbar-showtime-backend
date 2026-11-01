package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.MovieDetail;

public interface GetMovieDetail {

  MovieDetail execute(long tmdbId);
}
