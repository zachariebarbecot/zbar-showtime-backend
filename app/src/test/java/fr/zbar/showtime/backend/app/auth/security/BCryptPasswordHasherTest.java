package fr.zbar.showtime.backend.app.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.core.auth.application.AuthenticateUserService;
import fr.zbar.showtime.backend.core.user.domain.RawPassword;
import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

  private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

  @Test
  void should_use_a_dummy_hash_that_costs_as_much_as_a_real_one() {
    String realHash = hasher.hash(new RawPassword("Str0ng!Passw0rd")).value();

    String dummyHash = AuthenticateUserService.DUMMY_HASH.value();

    assertThat(dummyHash).hasSize(realHash.length());
    assertThat(dummyHash.substring(4, 7)).isEqualTo(realHash.substring(4, 7));
    assertThat(
            hasher.matches(new RawPassword("Str0ng!Passw0rd"), AuthenticateUserService.DUMMY_HASH))
        .isFalse();
  }
}
