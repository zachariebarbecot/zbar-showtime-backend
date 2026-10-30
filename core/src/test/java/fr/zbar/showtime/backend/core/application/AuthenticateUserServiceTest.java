package fr.zbar.showtime.backend.core.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.core.domain.BirthDate;
import fr.zbar.showtime.backend.core.domain.Email;
import fr.zbar.showtime.backend.core.domain.FullName;
import fr.zbar.showtime.backend.core.domain.Gender;
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
  private static final PasswordHasher HASHER = raw -> new PasswordHash("hashed:" + raw.value());
  private static final PasswordVerifier VERIFIER =
      (raw, hash) -> hash.equals(new PasswordHash("hashed:" + raw.value()));
  private static final TokenIssuer TOKEN_ISSUER = userId -> "token-" + userId;

  private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
  private final AuthenticateUserService authenticateUserService =
      new AuthenticateUserService(userRepository, VERIFIER, HASHER, TOKEN_ISSUER);

  private User registeredUser() {
    User user =
        User.register(
            new FullName("Dupont", "Jean"),
            new Email("jean@mail.fr"),
            new RawPassword(PASSWORD),
            HASHER,
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
}
