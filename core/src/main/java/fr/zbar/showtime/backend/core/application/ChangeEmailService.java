package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class ChangeEmailService implements ChangeEmail {

  private final UserRepository userRepository;
  private final PasswordVerifier passwordVerifier;
  private final Clock clock;

  @Override
  public User execute(ChangeEmailCommand command) {
    return null;
  }
}
