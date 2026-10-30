package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserRepository;
import org.springframework.stereotype.Component;

@Component
class UserPersistenceAdapter implements UserRepository {

  @Override
  public void save(User user) {}
}
