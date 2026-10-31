package fr.zbar.showtime.backend.app.persistence;

import fr.zbar.showtime.backend.app.user.persistence.UserJpaRepository;
import fr.zbar.showtime.backend.core.domain.LoginHistory;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class LoginHistoryPersistenceAdapter implements LoginHistory {

  private final LoginJpaRepository loginJpaRepository;
  private final UserJpaRepository userJpaRepository;

  @Override
  public void record(UUID userId, Instant loggedAt) {
    loginJpaRepository.saveAndFlush(
        new LoginJpaEntity(
            UUID.randomUUID(), userJpaRepository.getReferenceById(userId), loggedAt));
  }
}
