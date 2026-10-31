package fr.zbar.showtime.backend.core.application;

import static fr.zbar.showtime.backend.core.application.NamedField.named;

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
    RawPassword currentPassword =
        named("currentPassword", () -> new RawPassword(command.currentPassword()));
    RawPassword newPassword = named("newPassword", () -> new RawPassword(command.newPassword()));
    User user = userRepository.findById(command.userId()).orElseThrow(UserNotFoundException::new);
    userRepository.save(
        named(
            "newPassword",
            () ->
                user.changePassword(
                    currentPassword, newPassword, passwordVerifier, passwordHasher, clock)));
  }
}
