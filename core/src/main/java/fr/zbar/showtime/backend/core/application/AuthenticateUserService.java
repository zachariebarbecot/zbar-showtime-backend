package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.Email;
import fr.zbar.showtime.backend.core.domain.InvalidCredentialsException;
import fr.zbar.showtime.backend.core.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.domain.PasswordVerifier;
import fr.zbar.showtime.backend.core.domain.RawPassword;
import fr.zbar.showtime.backend.core.domain.TokenIssuer;
import fr.zbar.showtime.backend.core.domain.User;
import fr.zbar.showtime.backend.core.domain.UserRepository;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class AuthenticateUserService implements AuthenticateUser {

  private final UserRepository userRepository;
  private final PasswordVerifier passwordVerifier;
  private final PasswordHasher passwordHasher;
  private final TokenIssuer tokenIssuer;

  @Override
  public String execute(AuthenticateUserCommand command) {
    User user = userRepository.findByEmail(new Email(command.email())).orElseThrow();
    if (!passwordVerifier.matches(new RawPassword(command.password()), user.passwordHash())) {
      throw new InvalidCredentialsException();
    }
    return tokenIssuer.issue(user.id());
  }
}
