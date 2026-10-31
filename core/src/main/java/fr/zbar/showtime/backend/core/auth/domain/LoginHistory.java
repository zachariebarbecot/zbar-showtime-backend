package fr.zbar.showtime.backend.core.auth.domain;

import java.time.Instant;
import java.util.UUID;

public interface LoginHistory {

  void record(UUID userId, Instant loggedAt);
}
