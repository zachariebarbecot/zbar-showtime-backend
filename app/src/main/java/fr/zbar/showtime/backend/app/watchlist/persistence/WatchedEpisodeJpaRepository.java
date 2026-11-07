package fr.zbar.showtime.backend.app.watchlist.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchedEpisodeJpaRepository extends JpaRepository<WatchedEpisodeJpaEntity, UUID> {

  boolean existsByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
      UUID userId, long seriesTmdbId, int seasonNumber, int episodeNumber);
}
