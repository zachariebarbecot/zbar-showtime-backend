package fr.zbar.showtime.backend.app.persistence;

import fr.zbar.showtime.backend.core.domain.Email;
import fr.zbar.showtime.backend.core.domain.EmailAlreadyUsedException;
import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class UserPersistenceAdapter implements UserRepository {

  private static final String EMAIL_UNIQUE_CONSTRAINT = "UC_USERSEMAIL_COL";

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
      if (violatesEmailUniqueness(exception)) {
        throw new EmailAlreadyUsedException();
      }
      throw exception;
    }
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return Optional.empty();
  }

  private static boolean violatesEmailUniqueness(DataIntegrityViolationException exception) {
    return exception.getCause() instanceof ConstraintViolationException violation
        && EMAIL_UNIQUE_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName());
  }
}
