package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisode;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisodeRepository;
import java.util.List;
import java.util.Optional;
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

  @Override
  public boolean existsByUserIdAndSeriesTmdbId(UUID userId, long seriesTmdbId) {
    return episodes.stream()
        .anyMatch(
            episode -> episode.userId().equals(userId) && episode.seriesTmdbId() == seriesTmdbId);
  }

  @Override
  public long countByUserIdAndSeriesTmdbIdExcludingSpecials(UUID userId, long seriesTmdbId) {
    return episodes.stream()
        .filter(
            episode ->
                episode.userId().equals(userId)
                    && episode.seriesTmdbId() == seriesTmdbId
                    && episode.seasonNumber() > 0)
        .count();
  }

  @Override
  public Optional<WatchedEpisode> findByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
      UUID userId, long seriesTmdbId, int seasonNumber, int episodeNumber) {
    return episodes.stream()
        .filter(
            episode ->
                episode.userId().equals(userId)
                    && episode.seriesTmdbId() == seriesTmdbId
                    && episode.seasonNumber() == seasonNumber
                    && episode.episodeNumber() == episodeNumber)
        .findFirst();
  }

  @Override
  public void delete(WatchedEpisode episode) {
    episodes.remove(episode);
  }

  @Override
  public void deleteByUserIdAndSeriesTmdbId(UUID userId, long seriesTmdbId) {
    episodes.removeIf(
        episode -> episode.userId().equals(userId) && episode.seriesTmdbId() == seriesTmdbId);
  }

  @Override
  public List<WatchedEpisode> findByUserIdAndSeriesTmdbIdAndSeasonNumber(
      UUID userId, long seriesTmdbId, int seasonNumber) {
    return episodes.stream()
        .filter(
            episode ->
                episode.userId().equals(userId)
                    && episode.seriesTmdbId() == seriesTmdbId
                    && episode.seasonNumber() == seasonNumber)
        .toList();
  }
}
