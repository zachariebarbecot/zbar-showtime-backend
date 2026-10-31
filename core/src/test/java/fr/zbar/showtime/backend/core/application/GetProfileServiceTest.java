package fr.zbar.showtime.backend.core.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.core.domain.BirthDate;
import fr.zbar.showtime.backend.core.domain.Email;
import fr.zbar.showtime.backend.core.domain.FullName;
import fr.zbar.showtime.backend.core.domain.Gender;
import fr.zbar.showtime.backend.core.domain.PasswordHash;
import fr.zbar.showtime.backend.core.domain.PasswordHasher;
import fr.zbar.showtime.backend.core.domain.RawPassword;
import fr.zbar.showtime.backend.core.domain.User;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class GetProfileServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-03-01T10:15:30Z"), ZoneOffset.UTC);
  private final PasswordHasher hasher = raw -> new PasswordHash("hashed:" + raw.value());

  private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
  private final GetProfileService getProfileService = new GetProfileService(userRepository);

  private User registeredUser(String email) {
    User user =
        User.register(
            new FullName("Dupont", "Jean"),
            new Email(email),
            new RawPassword("Str0ng!Passw0rd"),
            hasher,
            new BirthDate(LocalDate.of(1990, 5, 17), CLOCK),
            Gender.MALE,
            CLOCK);
    userRepository.save(user);
    return user;
  }

  @Test
  void should_return_the_user_of_the_id_when_the_user_exists() {
    registeredUser("other@mail.fr");
    User user = registeredUser("jean@mail.fr");

    User profile = getProfileService.execute(user.id());

    assertThat(profile).isEqualTo(user);
    assertThat(profile.email()).isEqualTo(new Email("jean@mail.fr"));
  }
}
