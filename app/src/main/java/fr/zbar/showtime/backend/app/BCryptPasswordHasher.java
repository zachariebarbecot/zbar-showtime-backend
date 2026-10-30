package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.domain.PasswordHash;
import fr.zbar.showtime.backend.core.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.domain.RawPassword;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BCryptPasswordHasher implements PasswordHasher, PasswordVerifier {

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public PasswordHash hash(RawPassword password) {
    return new PasswordHash(encoder.encode(password.value()));
  }

  @Override
  public boolean matches(RawPassword password, PasswordHash hash) {
    return encoder.matches(password.value(), hash.value());
  }
}
