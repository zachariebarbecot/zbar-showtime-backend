package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class GetProfileService implements GetProfile {

  private final UserRepository userRepository;

  @Override
  public User execute(UUID userId) {
    return null;
  }
}
