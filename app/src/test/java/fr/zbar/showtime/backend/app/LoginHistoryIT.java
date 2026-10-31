package fr.zbar.showtime.backend.app;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.core.domain.LoginHistory;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class LoginHistoryIT extends AbstractIT {

  @Autowired private LoginHistory loginHistory;

  @Test
  void should_refuse_a_trace_for_an_unknown_user() {
    assertThatThrownBy(() -> loginHistory.record(UUID.randomUUID(), Instant.now()))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
