package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.MovieDetailCatalog;
import fr.zbar.showtime.backend.core.search.domain.TmdbId;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovieRepository;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class GetMovieDetailService implements GetMovieDetail {

  private final MovieDetailCatalog movieDetailCatalog;
  private final WatchlistMovieRepository watchlistMovieRepository;

  @Override
  public MovieDetailView execute(GetMovieDetailCommand command) {
    new TmdbId(command.tmdbId());
    String watchlistStatus =
        watchlistMovieRepository
            .findByUserIdAndTmdbId(command.userId(), command.tmdbId())
            .map(movie -> movie.status().name())
            .orElse(null);
    return new MovieDetailView(
        movieDetailCatalog.findMovieDetail(command.tmdbId()), watchlistStatus);
  }
}
