package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovieRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class AddMovieToWatchlistService implements AddMovieToWatchlist {

  private final TitleCatalog titleCatalog;
  private final WatchlistMovieRepository watchlistMovieRepository;
  private final Clock clock;

  @Override
  public WatchlistMovie execute(AddMovieToWatchlistCommand command) {
    titleCatalog.findMovie(command.tmdbId());
    WatchlistMovie movie = WatchlistMovie.add(command.userId(), command.tmdbId(), clock);
    watchlistMovieRepository.save(movie);
    return movie;
  }
}
