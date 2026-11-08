package fr.zbar.showtime.backend.app.watchlist.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WatchedEpisodeJpaRepository extends JpaRepository<WatchedEpisodeJpaEntity, UUID> {

  boolean existsByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
      UUID userId, long seriesTmdbId, int seasonNumber, int episodeNumber);

  Optional<WatchedEpisodeJpaEntity> findByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
      UUID userId, long seriesTmdbId, int seasonNumber, int episodeNumber);

  @Modifying
  @Query(
      "delete from WatchedEpisodeJpaEntity w"
          + " where w.user.id = :userId and w.seriesTmdbId = :seriesTmdbId")
  void deleteByUserIdAndSeriesTmdbId(UUID userId, long seriesTmdbId);
}
