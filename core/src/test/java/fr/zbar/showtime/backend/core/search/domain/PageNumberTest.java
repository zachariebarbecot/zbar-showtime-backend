package fr.zbar.showtime.backend.core.search.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PageNumberTest {

  @ParameterizedTest
  @ValueSource(ints = {1, 250, 500})
  void should_accept_a_page_between_1_and_500(int value) {
    assertThat(new PageNumber(value).value()).isEqualTo(value);
  }

  @ParameterizedTest
  @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 501, Integer.MAX_VALUE})
  void should_refuse_a_page_out_of_range(int value) {
    assertThatThrownBy(() -> new PageNumber(value))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("page");
              assertThat(exception.messages()).containsExactly("Page must be between 1 and 500");
            });
  }
}
