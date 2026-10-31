package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.Email;
import fr.zbar.showtime.backend.core.domain.InvalidCredentialsException;
import fr.zbar.showtime.backend.core.domain.InvalidFieldException;
import fr.zbar.showtime.backend.core.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.domain.RawPassword;
import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserNotFoundException;
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
    User user = userRepository.findById(command.userId()).orElseThrow(UserNotFoundException::new);
    Email newEmail = newEmail(command.newEmail());
    if (command.currentPassword() == null) {
      throw new InvalidFieldException("currentPassword", "Current password must be provided");
    }
    if (!passwordVerifier.matches(
        new RawPassword(command.currentPassword()), user.passwordHash())) {
      throw new InvalidCredentialsException();
    }
    User changed = user.changeEmail(newEmail, clock);
    userRepository.save(changed);
    return changed;
  }

  private static Email newEmail(String value) {
    try {
      return new Email(value);
    } catch (InvalidFieldException exception) {
      throw new InvalidFieldException("newEmail", exception.messages());
    }
  }
}
