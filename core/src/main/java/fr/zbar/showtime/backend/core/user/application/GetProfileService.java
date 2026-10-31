package fr.zbar.showtime.backend.core.user.application;

import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.user.domain.User;
import fr.zbar.showtime.backend.core.user.domain.UserNotFoundException;
import fr.zbar.showtime.backend.core.user.domain.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class GetProfileService implements GetProfile {

  private final UserRepository userRepository;

  @Override
  public User execute(UUID userId) {
    return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
  }
}
