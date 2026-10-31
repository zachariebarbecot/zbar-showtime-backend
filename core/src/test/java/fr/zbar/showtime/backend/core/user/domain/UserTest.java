package fr.zbar.showtime.backend.core.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.domain.InvalidCredentialsException;
import fr.zbar.showtime.backend.core.domain.PasswordHash;
import fr.zbar.showtime.backend.core.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.domain.RawPassword;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
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

  @Test
  void should_return_the_same_user_when_renamed_with_the_current_name() {
    User user = register();

    User renamed =
        user.rename(
            new FullName("Dupont", "Jean"), Clock.fixed(NOW.plusSeconds(60), ZoneOffset.UTC));

    assertThat(renamed).isSameAs(user);
    assertThat(renamed.updatedAt()).isEqualTo(NOW);
  }

  @Test
  void should_replace_email_and_update_date_keeping_the_rest_when_email_differs() {
    User user = register();
    Instant later = NOW.plusSeconds(3600);

    User changed = user.changeEmail(new Email("new@b.fr"), Clock.fixed(later, ZoneOffset.UTC));

    assertThat(changed.email()).isEqualTo(new Email("new@b.fr"));
    assertThat(changed.updatedAt()).isEqualTo(later);
    assertThat(changed.id()).isEqualTo(user.id());
    assertThat(changed.createdAt()).isEqualTo(NOW);
    assertThat(changed.name()).isEqualTo(user.name());
    assertThat(changed.passwordHash()).isEqualTo(user.passwordHash());
    assertThat(changed.birthDate()).isEqualTo(user.birthDate());
    assertThat(changed.gender()).isEqualTo(user.gender());
  }

  @Test
  void should_return_the_same_user_when_email_is_unchanged() {
    User user = register();

    User changed =
        user.changeEmail(new Email(" A@B.fr "), Clock.fixed(NOW.plusSeconds(3600), ZoneOffset.UTC));

    assertThat(changed).isSameAs(user);
    assertThat(changed.updatedAt()).isEqualTo(NOW);
  }

  private static final PasswordVerifier VERIFIER =
      (raw, hash) -> hash.equals(new PasswordHash("hashed:" + raw.value()));
  private static final RawPassword CURRENT = new RawPassword("Str0ng!Passw0rd");

  @Test
  void should_return_user_with_new_hash_and_clock_date_when_password_is_changed() {
    User user = register();
    Clock later = Clock.fixed(NOW.plusSeconds(3600), ZoneOffset.UTC);

    User changed =
        user.changePassword(CURRENT, new RawPassword("N3w!Passw0rdXyz"), VERIFIER, HASHER, later);

    assertThat(changed.passwordHash()).isEqualTo(new PasswordHash("hashed:N3w!Passw0rdXyz"));
    assertThat(changed.updatedAt()).isEqualTo(NOW.plusSeconds(3600));
    assertThat(changed.id()).isEqualTo(user.id());
    assertThat(changed.createdAt()).isEqualTo(NOW);
    assertThat(changed.email()).isEqualTo(user.email());
  }

  @Test
  void should_refuse_password_change_when_current_password_is_wrong() {
    User user = register();

    assertThatThrownBy(
            () ->
                user.changePassword(
                    new RawPassword("Wr0ng!Passw0rd"),
                    new RawPassword("N3w!Passw0rdXyz"),
                    VERIFIER,
                    HASHER,
                    CLOCK))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void should_refuse_new_password_when_it_equals_the_current_one() {
    User user = register();

    assertThatThrownBy(() -> user.changePassword(CURRENT, CURRENT, VERIFIER, HASHER, CLOCK))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("password");
              assertThat(exception.messages())
                  .contains("New password must differ from the current one");
            });
  }

  @Test
  void should_refuse_new_password_when_it_violates_the_policy() {
    User user = register();

    assertThatThrownBy(
            () -> user.changePassword(CURRENT, new RawPassword("short"), VERIFIER, HASHER, CLOCK))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("password");
              assertThat(exception.messages()).contains("Password must be at least 12 characters");
            });
  }

  @Test
  void should_refuse_new_password_when_it_contains_the_current_email() {
    User user = register();

    assertThatThrownBy(
            () ->
                user.changePassword(
                    CURRENT, new RawPassword("Xx!a@b.fr-1234"), VERIFIER, HASHER, CLOCK))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception ->
                assertThat(exception.messages()).contains("Password must not contain the email"));
  }

  @Test
  void should_accept_the_password_of_the_user_when_it_is_required() {
    register().requirePassword(CURRENT, VERIFIER);
  }

  @Test
  void should_refuse_a_password_that_is_not_the_one_of_the_user_when_it_is_required() {
    User user = register();

    assertThatThrownBy(() -> user.requirePassword(new RawPassword("Wr0ng!Passw0rd"), VERIFIER))
        .isInstanceOf(InvalidCredentialsException.class);
  }
}
