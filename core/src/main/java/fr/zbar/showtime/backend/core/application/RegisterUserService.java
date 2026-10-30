package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.BirthDate;
import fr.zbar.showtime.backend.core.domain.Email;
import fr.zbar.showtime.backend.core.domain.FullName;
import fr.zbar.showtime.backend.core.domain.Gender;
import fr.zbar.showtime.backend.core.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.domain.RawPassword;
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
  public void execute(RegisterUserCommand command) {
    userRepository.save(
        User.register(
            new FullName(command.lastName(), command.firstName()),
            new Email(command.email()),
            new RawPassword(command.password()),
            passwordHasher,
            new BirthDate(command.birthDate(), clock),
            Gender.from(command.gender()),
            clock));
  }
}
