package fr.zbar.showtime.backend.core.user.application;

import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.user.domain.FullName;
import fr.zbar.showtime.backend.core.user.domain.User;
import fr.zbar.showtime.backend.core.user.domain.UserNotFoundException;
import fr.zbar.showtime.backend.core.user.domain.UserRepository;
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
    userRepository.save(renamed);
    return renamed;
  }
}
