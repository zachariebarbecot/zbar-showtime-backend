package fr.zbar.showtime.backend.core.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.domain.Email;
import fr.zbar.showtime.backend.core.domain.Gender;
import fr.zbar.showtime.backend.core.domain.InvalidFieldException;
import fr.zbar.showtime.backend.core.domain.PasswordHash;
import fr.zbar.showtime.backend.core.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.domain.User;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegisterUserServiceTest {

  private static final Instant NOW = Instant.parse("2026-03-01T10:15:30Z");
  private static final PasswordHasher HASHER = raw -> new PasswordHash("hashed:" + raw.value());

  private final List<User> savedUsers = new ArrayList<>();
  private final RegisterUserService registerUserService =
      new RegisterUserService(savedUsers::add, HASHER, Clock.fixed(NOW, ZoneOffset.UTC));

  @Test
  void should_save_and_return_user_with_hashed_password_when_command_is_valid() {
    RegisterUserCommand command =
        new RegisterUserCommand(
            "Dupont", "Jean", "a@b.fr", "Str0ng!Passw0rd", LocalDate.of(1990, 5, 17), "HOMME");

    User user = registerUserService.execute(command);

    assertThat(savedUsers).containsExactly(user);
    assertThat(user.email()).isEqualTo(new Email("a@b.fr"));
    assertThat(user.gender()).isEqualTo(Gender.HOMME);
    assertThat(user.passwordHash()).isEqualTo(new PasswordHash("hashed:Str0ng!Passw0rd"));
    assertThat(user.createdAt()).isEqualTo(NOW).isEqualTo(user.updatedAt());
  }

  @Test
  void should_not_save_user_when_password_violates_the_policy() {
    RegisterUserCommand command =
        new RegisterUserCommand(
            "Dupont", "Jean", "a@b.fr", "short", LocalDate.of(1990, 5, 17), "HOMME");

    assertThatThrownBy(() -> registerUserService.execute(command))
        .isInstanceOf(InvalidFieldException.class);
    assertThat(savedUsers).isEmpty();
  }
}
