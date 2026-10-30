package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.Email;
import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class InMemoryUserRepository implements UserRepository {

  private final List<User> users;

  InMemoryUserRepository() {
    this(new ArrayList<>());
  }

  InMemoryUserRepository(List<User> users) {
    this.users = users;
  }

  @Override
  public void save(User user) {
    users.add(user);
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return users.stream().filter(user -> user.email().equals(email)).findFirst();
  }
}
