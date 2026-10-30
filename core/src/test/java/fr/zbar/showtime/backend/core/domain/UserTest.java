package fr.zbar.showtime.backend.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class UserTest {

  private static final Instant NOW = Instant.parse("2026-03-01T10:15:30Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
  private static final PasswordHasher HASHER = raw -> new PasswordHash("hashed:" + raw.value());

  private User register() {
    return User.register(
        new FullName("Dupont", "Jean"),
        new Email("a@b.fr"),
        new RawPassword("Str0ng!Passw0rd"),
        HASHER,
        new BirthDate(LocalDate.of(1990, 5, 17)),
        Gender.HOMME,
        CLOCK);
  }

  @Test
  void should_set_creation_and_update_dates_to_clock_instant_when_registered() {
    User user = register();

    assertThat(user.createdAt()).isEqualTo(NOW);
    assertThat(user.updatedAt()).isEqualTo(NOW);
  }

  @Test
  void should_keep_hash_instead_of_clear_password_when_registered() {
    User user = register();

    assertThat(user.passwordHash()).isEqualTo(new PasswordHash("hashed:Str0ng!Passw0rd"));
  }

  @Test
  void should_have_an_id_and_given_data_when_registered() {
    User user = register();

    assertThat(user.id()).isNotNull();
    assertThat(user.name()).isEqualTo(new FullName("Dupont", "Jean"));
    assertThat(user.email()).isEqualTo(new Email("a@b.fr"));
    assertThat(user.birthDate()).isEqualTo(new BirthDate(LocalDate.of(1990, 5, 17)));
    assertThat(user.gender()).isEqualTo(Gender.HOMME);
  }

  @Test
  void should_refuse_registration_when_password_violates_the_policy() {
    assertThatThrownBy(
            () ->
                User.register(
                    new FullName("Dupont", "Jean"),
                    new Email("jean@mail.fr"),
                    new RawPassword("short"),
                    HASHER,
                    new BirthDate(LocalDate.of(1990, 5, 17)),
                    Gender.HOMME,
                    CLOCK))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("password");
              assertThat(exception.messages())
                  .contains(
                      "Password must be at least 12 characters",
                      "Password must contain an uppercase letter");
            });
  }
}
