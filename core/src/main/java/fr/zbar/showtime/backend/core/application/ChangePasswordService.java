package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.InvalidCredentialsException;
import fr.zbar.showtime.backend.core.domain.InvalidFieldException;
import fr.zbar.showtime.backend.core.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.domain.RawPassword;
import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserNotFoundException;
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
  public void execute(ChangePasswordCommand command) {
    RawPassword currentPassword = rawPassword(command.currentPassword(), "currentPassword");
    RawPassword newPassword = rawPassword(command.newPassword(), "newPassword");
    User user = userRepository.findById(command.userId()).orElseThrow(UserNotFoundException::new);
    if (!passwordVerifier.matches(currentPassword, user.passwordHash())) {
      throw new InvalidCredentialsException();
    }
    if (passwordVerifier.matches(newPassword, user.passwordHash())) {
      throw new InvalidFieldException(
          "newPassword", "New password must differ from the current one");
    }
    userRepository.save(user.changePassword(newPassword, passwordHasher, clock));
  }

  private static RawPassword rawPassword(String value, String field) {
    if (value == null) {
      throw new InvalidFieldException(field, "Password must be provided");
    }
    return new RawPassword(value);
  }
}
