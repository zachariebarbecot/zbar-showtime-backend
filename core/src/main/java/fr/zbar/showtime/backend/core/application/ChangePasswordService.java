package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.domain.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class ChangePasswordService implements ChangePassword {

  private final UserRepository userRepository;
  private final PasswordVerifier passwordVerifier;
  private final PasswordHasher passwordHasher;
  private final Clock clock;

  @Override
  public void execute(ChangePasswordCommand command) {}
}
