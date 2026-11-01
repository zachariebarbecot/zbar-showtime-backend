package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistEntryNotFoundException;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovieRepository;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class RemoveMovieFromWatchlistService implements RemoveMovieFromWatchlist {

  private final WatchlistMovieRepository watchlistMovieRepository;

  @Override
  public void execute(RemoveMovieFromWatchlistCommand command) {
    WatchlistMovie movie =
        watchlistMovieRepository
            .findByUserIdAndTmdbId(command.userId(), command.tmdbId())
            .orElseThrow(WatchlistEntryNotFoundException::new);
    watchlistMovieRepository.delete(movie);
  }
}
