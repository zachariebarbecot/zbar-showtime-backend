package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.TmdbId;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisode;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisodeRepository;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class GetSeasonDetailService implements GetSeasonDetail {

  private final SeasonCatalog seasonCatalog;
  private final WatchedEpisodeRepository watchedEpisodeRepository;

  @Override
  public SeasonDetailView execute(GetSeasonDetailCommand command) {
    long tmdbId = new TmdbId(command.tmdbId()).value();
    if (command.seasonNumber() < 0) {
      throw new InvalidFieldException("seasonNumber", "Season number must not be negative");
    }
    Map<Integer, Instant> watchedAtByEpisode =
        watchedEpisodeRepository
            .findByUserIdAndSeriesTmdbIdAndSeasonNumber(
                command.userId(), tmdbId, command.seasonNumber())
            .stream()
            .collect(Collectors.toMap(WatchedEpisode::episodeNumber, WatchedEpisode::watchedAt));
    return new SeasonDetailView(
        seasonCatalog.findSeason(tmdbId, command.seasonNumber()), watchedAtByEpisode);
  }
}
