package fr.zbar.showtime.backend.core.watchlist.domain;

import java.util.UUID;

public interface WatchedEpisodeRepository {

  void save(WatchedEpisode episode);

  boolean existsByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
      UUID userId, long seriesTmdbId, int seasonNumber, int episodeNumber);
}
