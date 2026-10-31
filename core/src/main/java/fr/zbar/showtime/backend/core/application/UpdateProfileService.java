package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.FullName;
import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserNotFoundException;
import fr.zbar.showtime.backend.core.domain.UserRepository;
import java.time.Clock;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class UpdateProfileService implements UpdateProfile {

  private final UserRepository userRepository;
  private final Clock clock;

  @Override
  public User execute(UpdateProfileCommand command) {
    User user = userRepository.findById(command.userId()).orElseThrow(UserNotFoundException::new);
    FullName newName =
        new FullName(
            Objects.requireNonNullElse(command.lastName(), user.name().lastName()),
            Objects.requireNonNullElse(command.firstName(), user.name().firstName()));
    User renamed = user.rename(newName, clock);
    if (renamed != user) {
      userRepository.save(renamed);
    }
    return renamed;
  }
}
