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

class UpdateProfileServiceTest {

  private static final Instant REGISTERED_AT = Instant.parse("2026-03-01T10:15:30Z");
  private static final Instant NOW = Instant.parse("2026-03-02T08:00:00Z");
  private final PasswordHasher hasher = raw -> new PasswordHash("hashed:" + raw.value());

  private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
  private final UpdateProfileService updateProfileService =
      new UpdateProfileService(userRepository, Clock.fixed(NOW, ZoneOffset.UTC));

  private User registeredUser(String email) {
    User user =
        User.register(
            new FullName("Dupont", "Jean"),
            new Email(email),
            new RawPassword("Str0ng!Passw0rd"),
            hasher,
            new BirthDate(LocalDate.of(1990, 5, 17)),
            Gender.MALE,
            Clock.fixed(REGISTERED_AT, ZoneOffset.UTC));
    userRepository.save(user);
    return user;
  }

  @Test
  void should_save_and_return_the_renamed_user_when_the_name_changes() {
    registeredUser("other@mail.fr");
    User user = registeredUser("jean@mail.fr");

    User updated =
        updateProfileService.execute(new UpdateProfileCommand(user.id(), "Martin", "Paul"));

    assertThat(updated.name()).isEqualTo(new FullName("Martin", "Paul"));
    assertThat(updated.updatedAt()).isEqualTo(NOW);
    assertThat(userRepository.findById(user.id()).orElseThrow().name())
        .isEqualTo(new FullName("Martin", "Paul"));
    assertThat(userRepository.findById(user.id()).orElseThrow().updatedAt()).isEqualTo(NOW);
    assertThat(userRepository.findByEmail(new Email("other@mail.fr")).orElseThrow().name())
        .isEqualTo(new FullName("Dupont", "Jean"));
  }
}
