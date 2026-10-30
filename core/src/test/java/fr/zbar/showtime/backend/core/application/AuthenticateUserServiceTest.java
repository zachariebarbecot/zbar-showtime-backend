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
import fr.zbar.showtime.backend.core.domain.TokenIssuer;
import fr.zbar.showtime.backend.core.domain.User;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class AuthenticateUserServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-03-01T10:15:30Z"), ZoneOffset.UTC);
  private static final String PASSWORD = "Str0ng!Passw0rd";
  private final PasswordHasher hasher = raw -> new PasswordHash("hashed:" + raw.value());
  private static final PasswordVerifier VERIFIER =
      (raw, hash) -> hash.equals(new PasswordHash("hashed:" + raw.value()));
  private static final TokenIssuer TOKEN_ISSUER = userId -> "token-" + userId;

  private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
  private final AuthenticateUserService authenticateUserService =
      new AuthenticateUserService(userRepository, VERIFIER, TOKEN_ISSUER);

  private User registeredUser() {
    User user =
        User.register(
            new FullName("Dupont", "Jean"),
            new Email("jean@mail.fr"),
            new RawPassword(PASSWORD),
            hasher,
            new BirthDate(LocalDate.of(1990, 5, 17), CLOCK),
            Gender.MALE,
            CLOCK);
    userRepository.save(user);
    return user;
  }

  @Test
  void should_return_token_of_the_user_when_credentials_are_correct() {
    User user = registeredUser();

    String token =
        authenticateUserService.execute(new AuthenticateUserCommand("jean@mail.fr", PASSWORD));

    assertThat(token).isEqualTo("token-" + user.id());
  }

  @Test
  void should_reject_credentials_when_password_is_wrong() {
    registeredUser();

    assertThatThrownBy(
            () ->
                authenticateUserService.execute(
                    new AuthenticateUserCommand("jean@mail.fr", "Wr0ng!Passw0rd")))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Identifiants invalides");
  }

  @Test
  void should_reject_credentials_when_no_account_has_the_email() {
    assertThatThrownBy(
            () ->
                authenticateUserService.execute(
                    new AuthenticateUserCommand("unknown@mail.fr", PASSWORD)))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Identifiants invalides");
  }

  @Test
  void should_return_token_when_email_has_spaces_and_uppercase() {
    User user = registeredUser();

    String token =
        authenticateUserService.execute(new AuthenticateUserCommand(" Jean@Mail.FR ", PASSWORD));

    assertThat(token).isEqualTo("token-" + user.id());
  }
}
