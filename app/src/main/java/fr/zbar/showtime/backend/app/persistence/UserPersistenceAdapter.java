package fr.zbar.showtime.backend.app.persistence;

import fr.zbar.showtime.backend.core.domain.EmailAlreadyUsedException;
import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class UserPersistenceAdapter implements UserRepository {

  private final UserJpaRepository userJpaRepository;

  @Override
  public void save(User user) {
    try {
      userJpaRepository.saveAndFlush(
          new UserJpaEntity(
              user.id(),
              user.name().lastName(),
              user.name().firstName(),
              user.email().value(),
              user.passwordHash().value(),
              user.birthDate().value(),
              user.gender().name(),
              user.createdAt(),
              user.updatedAt()));
    } catch (DataIntegrityViolationException exception) {
      throw new EmailAlreadyUsedException();
    }
  }
}
