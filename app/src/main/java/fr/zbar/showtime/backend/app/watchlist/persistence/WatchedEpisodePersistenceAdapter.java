package fr.zbar.showtime.backend.app.watchlist.persistence;

import fr.zbar.showtime.backend.app.user.persistence.UserJpaRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisode;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisodeRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class WatchedEpisodePersistenceAdapter implements WatchedEpisodeRepository {

  private final WatchedEpisodeJpaRepository watchedEpisodeJpaRepository;
  private final UserJpaRepository userJpaRepository;

  @Override
  public void save(WatchedEpisode episode) {
    watchedEpisodeJpaRepository.saveAndFlush(
        new WatchedEpisodeJpaEntity(
            episode.id(),
            userJpaRepository.getReferenceById(episode.userId()),
            episode.seriesTmdbId(),
            episode.seasonNumber(),
            episode.episodeNumber(),
            episode.watchedAt()));
  }

  @Override
  public boolean existsByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
      UUID userId, long seriesTmdbId, int seasonNumber, int episodeNumber) {
    return watchedEpisodeJpaRepository.existsByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
        userId, seriesTmdbId, seasonNumber, episodeNumber);
  }
}
