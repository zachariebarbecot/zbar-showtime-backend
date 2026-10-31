package fr.zbar.showtime.backend.app.auth.security;

import fr.zbar.showtime.backend.core.user.domain.PasswordHash;
import fr.zbar.showtime.backend.core.user.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.user.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.user.domain.RawPassword;
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
