package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.Email;
import fr.zbar.showtime.backend.core.domain.InvalidCredentialsException;
import fr.zbar.showtime.backend.core.domain.LoginHistory;
import fr.zbar.showtime.backend.core.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.domain.RawPassword;
import fr.zbar.showtime.backend.core.domain.TokenIssuer;
import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserRepository;
import java.time.Clock;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class AuthenticateUserService implements AuthenticateUser {

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
      throw new InvalidCredentialsException();
    }
    if (!passwordVerifier.matches(password, user.get().passwordHash())) {
      throw new InvalidCredentialsException();
    }
    return tokenIssuer.issue(user.get().id());
  }
}
