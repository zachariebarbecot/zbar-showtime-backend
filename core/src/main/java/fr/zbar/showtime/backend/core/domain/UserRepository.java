package fr.zbar.showtime.backend.core.domain;

import java.util.Optional;

public interface UserRepository {

  void save(User user);

  Optional<User> findByEmail(Email email);
}
