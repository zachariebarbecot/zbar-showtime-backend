package fr.zbar.showtime.backend.core.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.domain.BirthDate;
import fr.zbar.showtime.backend.core.domain.Email;
import fr.zbar.showtime.backend.core.domain.FullName;
import fr.zbar.showtime.backend.core.domain.Gender;
import fr.zbar.showtime.backend.core.domain.InvalidCredentialsException;
import fr.zbar.showtime.backend.core.domain.PasswordHash;
import fr.zbar.showtime.backend.core.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.domain.RawPassword;
import fr.zbar.showtime.backend.core.domain.User;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChangeEmailServiceTest {

  private static final Instant REGISTERED_AT = Instant.parse("2026-03-01T10:15:30Z");
  private static final Instant NOW = Instant.parse("2026-04-02T08:00:00Z");
  private static final Clock REGISTRATION_CLOCK = Clock.fixed(REGISTERED_AT, ZoneOffset.UTC);
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
  private static final String PASSWORD = "Str0ng!Passw0rd";
  private static final PasswordHasher HASHER = raw -> new PasswordHash("hashed:" + raw.value());
  private static final PasswordVerifier VERIFIER =
      (raw, hash) -> hash.equals(new PasswordHash("hashed:" + raw.value()));

  private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
  private final ChangeEmailService changeEmailService =
      new ChangeEmailService(userRepository, VERIFIER, CLOCK);

  private User registeredUser() {
    User user =
        User.register(
            new FullName("Dupont", "Jean"),
            new Email("jean@mail.fr"),
            new RawPassword(PASSWORD),
            HASHER,
            new BirthDate(LocalDate.of(1990, 5, 17), REGISTRATION_CLOCK),
            Gender.MALE,
            REGISTRATION_CLOCK);
    userRepository.save(user);
    return user;
  }

  @Test
  void should_save_the_normalized_email_and_update_date_when_the_password_is_correct() {
    User user = registeredUser();

    User changed =
        changeEmailService.execute(new ChangeEmailCommand(user.id(), PASSWORD, "  New@Mail.FR "));

    assertThat(changed.email()).isEqualTo(new Email("new@mail.fr"));
    assertThat(changed.updatedAt()).isEqualTo(NOW);
    assertThat(userRepository.findById(user.id())).contains(changed);
    assertThat(userRepository.findByEmail(new Email("jean@mail.fr"))).isEmpty();
    assertThat(userRepository.findById(UUID.randomUUID())).isEmpty();
  }

  @Test
  void should_reject_and_keep_the_email_when_the_current_password_is_wrong() {
    User user = registeredUser();

    assertThatThrownBy(
            () ->
                changeEmailService.execute(
                    new ChangeEmailCommand(user.id(), "Wr0ng!Passw0rd", "new@mail.fr")))
        .isInstanceOf(InvalidCredentialsException.class);

    assertThat(userRepository.findById(user.id())).contains(user);
  }
}
