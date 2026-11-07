package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisodeRepository;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class UnmarkEpisodeWatchedService implements UnmarkEpisodeWatched {

  private final WatchedEpisodeRepository watchedEpisodeRepository;

  @Override
  public void execute(UnmarkEpisodeWatchedCommand command) {
    watchedEpisodeRepository
        .findByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
            command.userId(), command.tmdbId(), command.seasonNumber(), command.episodeNumber())
        .ifPresent(watchedEpisodeRepository::delete);
  }
}
