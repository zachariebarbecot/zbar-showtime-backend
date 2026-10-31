package fr.zbar.showtime.backend.core.user.application;

import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.user.domain.BirthDate;
import fr.zbar.showtime.backend.core.user.domain.Email;
import fr.zbar.showtime.backend.core.user.domain.FullName;
import fr.zbar.showtime.backend.core.user.domain.Gender;
import fr.zbar.showtime.backend.core.user.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.user.domain.RawPassword;
import fr.zbar.showtime.backend.core.user.domain.User;
import fr.zbar.showtime.backend.core.user.domain.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUser {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final Clock clock;

  @Override
  public void execute(RegisterUserCommand command) {
    userRepository.save(
        User.register(
            new FullName(command.lastName(), command.firstName()),
            new Email(command.email()),
            new RawPassword(command.password()),
            passwordHasher,
            BirthDate.of(command.birthDate(), clock),
            Gender.from(command.gender()),
            clock));
  }
}
