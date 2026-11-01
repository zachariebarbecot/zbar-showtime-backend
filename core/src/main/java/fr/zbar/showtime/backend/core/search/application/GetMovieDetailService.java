package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.MovieDetailCatalog;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovieRepository;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class GetMovieDetailService implements GetMovieDetail {

  private final MovieDetailCatalog movieDetailCatalog;
  private final WatchlistMovieRepository watchlistMovieRepository;

  @Override
  public MovieDetailView execute(GetMovieDetailCommand command) {
    if (command.tmdbId() <= 0) {
      throw new InvalidFieldException("tmdbId", "TMDB id must be greater than 0");
    }
    return new MovieDetailView(movieDetailCatalog.findMovieDetail(command.tmdbId()), null);
  }
}
