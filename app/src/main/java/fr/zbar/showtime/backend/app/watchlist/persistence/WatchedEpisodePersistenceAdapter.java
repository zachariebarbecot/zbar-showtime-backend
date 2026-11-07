package fr.zbar.showtime.backend.app.watchlist.persistence;

import fr.zbar.showtime.backend.app.user.persistence.UserJpaRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisode;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisodeRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class WatchedEpisodePersistenceAdapter implements WatchedEpisodeRepository {

  private static final String UNIQUE_CONSTRAINT = "UC_watched_episode_user_series_season_episode";

  private final WatchedEpisodeJpaRepository watchedEpisodeJpaRepository;
  private final UserJpaRepository userJpaRepository;

  @Override
  public void save(WatchedEpisode episode) {
    try {
      watchedEpisodeJpaRepository.saveAndFlush(
          new WatchedEpisodeJpaEntity(
              episode.id(),
              userJpaRepository.getReferenceById(episode.userId()),
              episode.seriesTmdbId(),
              episode.seasonNumber(),
              episode.episodeNumber(),
              episode.watchedAt()));
    } catch (DataIntegrityViolationException exception) {
      if (!violatesUniqueness(exception)) {
        throw exception;
      }
    }
  }

  @Override
  public boolean existsByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
      UUID userId, long seriesTmdbId, int seasonNumber, int episodeNumber) {
    return watchedEpisodeJpaRepository.existsByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
        userId, seriesTmdbId, seasonNumber, episodeNumber);
  }

  @Override
  public Optional<WatchedEpisode> findByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
      UUID userId, long seriesTmdbId, int seasonNumber, int episodeNumber) {
    return Optional.empty();
  }

  @Override
  public void delete(WatchedEpisode episode) {}

  private static boolean violatesUniqueness(DataIntegrityViolationException exception) {
    return exception.getCause() instanceof ConstraintViolationException violation
        && UNIQUE_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName());
  }
}
