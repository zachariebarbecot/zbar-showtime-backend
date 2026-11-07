package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisode;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisodeRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistEntryNotFoundException;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class UnmarkEpisodeWatchedService implements UnmarkEpisodeWatched {

  private final WatchedEpisodeRepository watchedEpisodeRepository;

  @Override
  public void execute(UnmarkEpisodeWatchedCommand command) {
    WatchedEpisode episode =
        watchedEpisodeRepository
            .findByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
                command.userId(), command.tmdbId(), command.seasonNumber(), command.episodeNumber())
            .orElseThrow(WatchlistEntryNotFoundException::new);
    watchedEpisodeRepository.delete(episode);
  }
}
