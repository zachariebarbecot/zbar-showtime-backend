package fr.zbar.showtime.backend.core.watchlist.domain;

import java.util.Optional;
import java.util.UUID;

public interface WatchedEpisodeRepository {

  void save(WatchedEpisode episode);

  boolean existsByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
      UUID userId, long seriesTmdbId, int seasonNumber, int episodeNumber);

  boolean existsByUserIdAndSeriesTmdbId(UUID userId, long seriesTmdbId);

  Optional<WatchedEpisode> findByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
      UUID userId, long seriesTmdbId, int seasonNumber, int episodeNumber);

  void delete(WatchedEpisode episode);

  void deleteByUserIdAndSeriesTmdbId(UUID userId, long seriesTmdbId);
}
