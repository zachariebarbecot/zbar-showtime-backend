package fr.zbar.showtime.backend.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SearchQueryTest {

  @Test
  void should_keep_the_trimmed_value_when_the_query_has_surrounding_spaces() {
    assertThat(new SearchQuery("  matrix  ").value()).isEqualTo("matrix");
  }

  @ParameterizedTest
  @ValueSource(strings = {"ab", " ab ", "L'été & co"})
  void should_accept_the_lower_bound(String value) {
    assertThat(new SearchQuery(value).value()).isEqualTo(value.strip());
  }

  @Test
  void should_accept_100_characters() {
    String value = "a".repeat(100);

    assertThat(new SearchQuery(value).value()).isEqualTo(value);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "   ", "a", " a "})
  void should_refuse_a_query_shorter_than_2_characters_after_trim(String value) {
    assertThatThrownBy(() -> new SearchQuery(value))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("query");
              assertThat(exception.messages())
                  .containsExactly("Query must be between 2 and 100 characters");
            });
  }

  @Test
  void should_refuse_a_query_longer_than_100_characters_after_trim() {
    String value = " " + "a".repeat(101) + " ";

    assertThatThrownBy(() -> new SearchQuery(value))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("query");
              assertThat(exception.messages())
                  .containsExactly("Query must be between 2 and 100 characters");
            });
  }
}
