package fr.zbar.showtime.backend.core.auth.application;

import fr.zbar.showtime.backend.core.auth.domain.LoginHistory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class InMemoryLoginHistory implements LoginHistory {

  record Login(UUID userId, Instant loggedAt) {}

  private final List<Login> logins = new ArrayList<>();

  @Override
  public void record(UUID userId, Instant loggedAt) {
    logins.add(new Login(userId, loggedAt));
  }

  List<Login> logins() {
    return logins;
  }
}
