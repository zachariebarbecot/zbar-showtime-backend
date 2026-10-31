package fr.zbar.showtime.backend.core.user.application;

import static fr.zbar.showtime.backend.core.shared.NamedField.named;

import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.user.domain.Email;
import fr.zbar.showtime.backend.core.user.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.user.domain.RawPassword;
import fr.zbar.showtime.backend.core.user.domain.User;
import fr.zbar.showtime.backend.core.user.domain.UserNotFoundException;
import fr.zbar.showtime.backend.core.user.domain.UserRepository;
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
    Email newEmail = named("newEmail", () -> new Email(command.newEmail()));
    RawPassword currentPassword =
        named("currentPassword", () -> new RawPassword(command.currentPassword()));
    user.requirePassword(currentPassword, passwordVerifier);
    User changed = user.changeEmail(newEmail, clock);
    userRepository.save(changed);
    return changed;
  }
}
