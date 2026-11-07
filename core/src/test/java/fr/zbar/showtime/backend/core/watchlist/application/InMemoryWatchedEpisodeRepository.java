package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisode;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisodeRepository;
import java.util.List;
import java.util.UUID;

public class InMemoryWatchedEpisodeRepository implements WatchedEpisodeRepository {

  private final List<WatchedEpisode> episodes;

  public InMemoryWatchedEpisodeRepository(List<WatchedEpisode> episodes) {
    this.episodes = episodes;
  }

  @Override
  public void save(WatchedEpisode episode) {
    episodes.add(episode);
  }

  @Override
  public boolean existsByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
      UUID userId, long seriesTmdbId, int seasonNumber, int episodeNumber) {
    return episodes.stream()
        .anyMatch(
            episode ->
                episode.userId().equals(userId)
                    && episode.seriesTmdbId() == seriesTmdbId
                    && episode.seasonNumber() == seasonNumber
                    && episode.episodeNumber() == episodeNumber);
  }
}
