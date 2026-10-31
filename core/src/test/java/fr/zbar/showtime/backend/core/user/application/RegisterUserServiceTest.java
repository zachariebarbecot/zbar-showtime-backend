package fr.zbar.showtime.backend.core.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import fr.zbar.showtime.backend.core.user.domain.Email;
import fr.zbar.showtime.backend.core.user.domain.Gender;
import fr.zbar.showtime.backend.core.user.domain.PasswordHash;
import fr.zbar.showtime.backend.core.user.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.user.domain.User;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RegisterUserServiceTest {

  private static final Instant NOW = Instant.parse("2026-03-01T10:15:30Z");
  private static final PasswordHasher HASHER = raw -> new PasswordHash("hashed:" + raw.value());

  private final List<User> savedUsers = new ArrayList<>();
  private final RegisterUserService registerUserService =
      new RegisterUserService(
          new InMemoryUserRepository(savedUsers), HASHER, Clock.fixed(NOW, ZoneOffset.UTC));

  @Test
  void should_save_user_with_hashed_password_when_command_is_valid() {
    RegisterUserCommand command =
        new RegisterUserCommand(
            "Dupont", "Jean", "a@b.fr", "Str0ng!Passw0rd", LocalDate.of(1990, 5, 17), "MALE");

    registerUserService.execute(command);

    assertThat(savedUsers).hasSize(1);
    User user = savedUsers.getFirst();
    assertThat(user.email()).isEqualTo(new Email("a@b.fr"));
    assertThat(user.gender()).isEqualTo(Gender.MALE);
    assertThat(user.passwordHash()).isEqualTo(new PasswordHash("hashed:Str0ng!Passw0rd"));
    assertThat(user.createdAt()).isEqualTo(NOW).isEqualTo(user.updatedAt());
  }

  @Test
  void should_not_save_user_when_password_violates_the_policy() {
    RegisterUserCommand command =
        new RegisterUserCommand(
            "Dupont", "Jean", "a@b.fr", "short", LocalDate.of(1990, 5, 17), "MALE");

    assertThatThrownBy(() -> registerUserService.execute(command))
        .isInstanceOf(InvalidFieldException.class);
    assertThat(savedUsers).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({
    "email, not-an-email, 1990-05-17, MALE, Dupont, Jean",
    "birthDate, a@b.fr, 2026-03-01, MALE, Dupont, Jean",
    "birthDate, a@b.fr, 2026-03-02, MALE, Dupont, Jean",
    "gender, a@b.fr, 1990-05-17, AUTRE, Dupont, Jean",
    "lastName, a@b.fr, 1990-05-17, MALE, ' ', Jean",
    "firstName, a@b.fr, 1990-05-17, MALE, Dupont, ' '"
  })
  void should_not_save_user_and_name_the_field_when_data_is_invalid(
      String field,
      String email,
      LocalDate birthDate,
      String gender,
      String lastName,
      String firstName) {
    RegisterUserCommand command =
        new RegisterUserCommand(lastName, firstName, email, "Str0ng!Passw0rd", birthDate, gender);

    assertThatThrownBy(() -> registerUserService.execute(command))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> assertThat(exception.field()).isEqualTo(field));
    assertThat(savedUsers).isEmpty();
  }
}
