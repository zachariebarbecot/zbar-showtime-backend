package fr.zbar.showtime.backend.core.user.application;

import fr.zbar.showtime.backend.core.user.domain.Email;
import fr.zbar.showtime.backend.core.user.domain.User;
import fr.zbar.showtime.backend.core.user.domain.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InMemoryUserRepository implements UserRepository {

  private final List<User> users;

  public InMemoryUserRepository() {
    this(new ArrayList<>());
  }

  public InMemoryUserRepository(List<User> users) {
    this.users = users;
  }

  @Override
  public void save(User user) {
    users.removeIf(existing -> existing.id().equals(user.id()));
    users.add(user);
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return users.stream().filter(user -> user.email().equals(email)).findFirst();
  }

  @Override
  public Optional<User> findById(UUID id) {
    return users.stream().filter(user -> user.id().equals(id)).findFirst();
  }
}
