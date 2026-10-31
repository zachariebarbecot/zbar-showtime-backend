package fr.zbar.showtime.backend.core.auth.application;

import fr.zbar.showtime.backend.core.auth.domain.LoginHistory;
import fr.zbar.showtime.backend.core.auth.domain.TokenIssuer;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.user.domain.Email;
import fr.zbar.showtime.backend.core.user.domain.InvalidCredentialsException;
import fr.zbar.showtime.backend.core.user.domain.PasswordHash;
import fr.zbar.showtime.backend.core.user.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.user.domain.RawPassword;
import fr.zbar.showtime.backend.core.user.domain.User;
import fr.zbar.showtime.backend.core.user.domain.UserRepository;
import java.time.Clock;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class AuthenticateUserService implements AuthenticateUser {

  public static final PasswordHash DUMMY_HASH =
      new PasswordHash("$2y$10$T9H53St9TXePDjkQHU2D/OD6kuv7yxdosNP5R9jk8qgcJqQkKYSni");

  private final UserRepository userRepository;
  private final PasswordVerifier passwordVerifier;
  private final TokenIssuer tokenIssuer;
  private final LoginHistory loginHistory;
  private final Clock clock;

  @Override
  public String execute(AuthenticateUserCommand command) {
    RawPassword password = new RawPassword(command.password());
    Optional<User> user = userRepository.findByEmail(new Email(command.email()));
    if (user.isEmpty()) {
      passwordVerifier.matches(password, DUMMY_HASH);
      throw new InvalidCredentialsException();
    }
    user.get().requirePassword(password, passwordVerifier);
    loginHistory.record(user.get().id(), clock.instant());
    return tokenIssuer.issue(user.get().id());
  }
}
