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
        Gender.MALE,
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
    assertThat(user.gender()).isEqualTo(Gender.MALE);
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
                    Gender.MALE,
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

  @Test
  void should_hide_personal_data_when_converted_to_string() {
    assertThat(register().toString())
        .doesNotContain("Dupont", "Jean", "a@b.fr", "1990", "hashed", "MALE");
  }

  @Test
  void should_be_equal_when_ids_are_equal() {
    User user = register();
    User sameId =
        new User(
            user.id(),
            new FullName("Martin", "Paul"),
            new Email("c@d.fr"),
            new PasswordHash("other"),
            new BirthDate(LocalDate.of(1980, 1, 1)),
            Gender.FEMALE,
            NOW,
            NOW);

    assertThat(sameId).isEqualTo(user).hasSameHashCodeAs(user);
    assertThat(register()).isNotEqualTo(user);
  }

  @Test
  void should_replace_the_name_and_set_update_date_to_clock_instant_when_renamed() {
    User user = register();
    Instant later = NOW.plusSeconds(3600);

    User renamed = user.rename(new FullName("Martin", "Paul"), Clock.fixed(later, ZoneOffset.UTC));

    assertThat(renamed.id()).isEqualTo(user.id());
    assertThat(renamed.name()).isEqualTo(new FullName("Martin", "Paul"));
    assertThat(renamed.updatedAt()).isEqualTo(later);
    assertThat(renamed.createdAt()).isEqualTo(NOW);
    assertThat(renamed.email()).isEqualTo(user.email());
    assertThat(renamed.passwordHash()).isEqualTo(user.passwordHash());
    assertThat(renamed.birthDate()).isEqualTo(user.birthDate());
    assertThat(renamed.gender()).isEqualTo(user.gender());
  }
}
