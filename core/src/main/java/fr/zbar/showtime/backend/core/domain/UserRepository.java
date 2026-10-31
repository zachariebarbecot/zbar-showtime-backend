package fr.zbar.showtime.backend.core.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

  void save(User user);

  Optional<User> findByEmail(Email email);

  Optional<User> findById(UUID id);
}
