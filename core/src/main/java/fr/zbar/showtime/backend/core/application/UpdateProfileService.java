package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.FullName;
import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserNotFoundException;
import fr.zbar.showtime.backend.core.domain.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class UpdateProfileService implements UpdateProfile {

  private final UserRepository userRepository;
  private final Clock clock;

  @Override
  public User execute(UpdateProfileCommand command) {
    User user = userRepository.findById(command.userId()).orElseThrow(UserNotFoundException::new);
    User renamed = user.rename(new FullName(command.lastName(), command.firstName()), clock);
    if (renamed != user) {
      userRepository.save(renamed);
    }
    return renamed;
  }
}
