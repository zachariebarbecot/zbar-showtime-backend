package fr.zbar.showtime.backend.core.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.domain.BirthDate;
import fr.zbar.showtime.backend.core.domain.Email;
import fr.zbar.showtime.backend.core.domain.FullName;
import fr.zbar.showtime.backend.core.domain.Gender;
import fr.zbar.showtime.backend.core.domain.InvalidCredentialsException;
import fr.zbar.showtime.backend.core.domain.InvalidFieldException;
import fr.zbar.showtime.backend.core.domain.PasswordHash;
import fr.zbar.showtime.backend.core.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.domain.RawPassword;
import fr.zbar.showtime.backend.core.domain.User;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ChangePasswordServiceTest {

  private static final Instant REGISTERED_AT = Instant.parse("2026-03-01T10:15:30Z");
  private static final Instant NOW = Instant.parse("2026-04-01T08:00:00Z");
  private static final Clock REGISTER_CLOCK = Clock.fixed(REGISTERED_AT, ZoneOffset.UTC);
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
  private static final String CURRENT = "Str0ng!Passw0rd";
  private static final String NEW = "N3w!Passw0rdXyz";
  private static final PasswordHasher HASHER = raw -> new PasswordHash("hashed:" + raw.value());
  private static final PasswordVerifier VERIFIER =
      (raw, hash) -> hash.equals(new PasswordHash("hashed:" + raw.value()));

  private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
  private final ChangePasswordService changePasswordService =
      new ChangePasswordService(userRepository, VERIFIER, HASHER, CLOCK);

  private User registeredUser() {
    User user =
        User.register(
            new FullName("Dupont", "Jean"),
            new Email("jean@mail.fr"),
            new RawPassword(CURRENT),
            HASHER,
            new BirthDate(LocalDate.of(1990, 5, 17), REGISTER_CLOCK),
            Gender.MALE,
            REGISTER_CLOCK);
    userRepository.save(user);
    return user;
  }

  @Test
  void should_store_the_new_hash_and_update_the_date_when_current_password_is_correct() {
    User user = registeredUser();

    changePasswordService.execute(new ChangePasswordCommand(user.id(), CURRENT, NEW));

    User saved = userRepository.findById(user.id()).orElseThrow();
    assertThat(saved.passwordHash()).isEqualTo(new PasswordHash("hashed:" + NEW));
    assertThat(saved.updatedAt()).isEqualTo(NOW);
    assertThat(saved.createdAt()).isEqualTo(REGISTERED_AT);
    assertThat(saved.email()).isEqualTo(user.email());
  }

  @Test
  void should_reject_credentials_and_keep_the_user_when_current_password_is_wrong() {
    User user = registeredUser();

    assertThatThrownBy(
            () ->
                changePasswordService.execute(
                    new ChangePasswordCommand(user.id(), "Wr0ng!Passw0rd", NEW)))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Invalid credentials");

    assertThat(userRepository.findById(user.id()).orElseThrow().passwordHash())
        .isEqualTo(user.passwordHash());
    assertThat(userRepository.findById(user.id()).orElseThrow().updatedAt())
        .isEqualTo(REGISTERED_AT);
  }

  @Test
  void should_reject_and_keep_the_user_when_new_password_violates_the_policy() {
    User user = registeredUser();

    assertThatThrownBy(
            () ->
                changePasswordService.execute(
                    new ChangePasswordCommand(user.id(), CURRENT, "short")))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> assertThat(exception.field()).isEqualTo("newPassword"));

    assertThat(userRepository.findById(user.id()).orElseThrow().passwordHash())
        .isEqualTo(user.passwordHash());
  }

  @Test
  void should_reject_and_keep_the_user_when_new_password_equals_the_current_one() {
    User user = registeredUser();

    assertThatThrownBy(
            () ->
                changePasswordService.execute(
                    new ChangePasswordCommand(user.id(), CURRENT, CURRENT)))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> assertThat(exception.field()).isEqualTo("newPassword"));

    assertThat(userRepository.findById(user.id()).orElseThrow().updatedAt())
        .isEqualTo(REGISTERED_AT);
  }

  @Test
  void should_reject_with_the_name_of_the_missing_password() {
    User user = registeredUser();

    assertThatThrownBy(
            () -> changePasswordService.execute(new ChangePasswordCommand(user.id(), null, NEW)))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> assertThat(exception.field()).isEqualTo("currentPassword"));
    assertThatThrownBy(
            () ->
                changePasswordService.execute(new ChangePasswordCommand(user.id(), CURRENT, null)))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> assertThat(exception.field()).isEqualTo("newPassword"));
  }
}
