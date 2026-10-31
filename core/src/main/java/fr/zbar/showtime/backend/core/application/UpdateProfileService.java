package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.User;
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
    return null;
  }
}
