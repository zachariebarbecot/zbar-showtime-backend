package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.domain.PasswordHash;
import fr.zbar.showtime.backend.core.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.domain.RawPassword;
import org.springframework.stereotype.Component;

@Component
class BCryptPasswordHasher implements PasswordHasher {

  @Override
  public PasswordHash hash(RawPassword password) {
    return null;
  }
}
