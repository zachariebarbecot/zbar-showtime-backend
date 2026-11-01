package fr.zbar.showtime.backend.core.search.application;

public interface GetMovieDetail {

  MovieDetailView execute(GetMovieDetailCommand command);
}
