package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUser {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final Clock clock;

  @Override
  public User execute(RegisterUserCommand command) {
    return null;
  }
}
