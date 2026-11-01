package fr.zbar.showtime.backend.app.watchlist.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistMovieJpaRepository extends JpaRepository<WatchlistMovieJpaEntity, UUID> {}
