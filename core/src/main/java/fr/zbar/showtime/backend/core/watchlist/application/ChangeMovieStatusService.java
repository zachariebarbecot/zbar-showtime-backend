package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistEntryNotFoundException;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovieRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class ChangeMovieStatusService implements ChangeMovieStatus {

  private final WatchlistMovieRepository watchlistMovieRepository;
  private final Clock clock;

  @Override
  public WatchlistMovie execute(ChangeMovieStatusCommand command) {
    WatchlistMovie movie =
        watchlistMovieRepository
            .findByUserIdAndTmdbId(command.userId(), command.tmdbId())
            .orElseThrow(WatchlistEntryNotFoundException::new);
    WatchlistMovie changed = movie.changeStatus(command.status(), clock);
    watchlistMovieRepository.save(changed);
    return changed;
  }
}
