package fr.zbar.showtime.backend.core.domain;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class User extends AggregateRoot<UUID> {

  FullName name;
  Email email;
  PasswordHash passwordHash;
  BirthDate birthDate;
  Gender gender;
  Instant createdAt;
  Instant updatedAt;

  public User(
      UUID id,
      FullName name,
      Email email,
      PasswordHash passwordHash,
      BirthDate birthDate,
      Gender gender,
      Instant createdAt,
      Instant updatedAt) {
    super(id);
    this.name = name;
    this.email = email;
    this.passwordHash = passwordHash;
    this.birthDate = birthDate;
    this.gender = gender;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static User register(
      FullName name,
      Email email,
      RawPassword password,
      PasswordHasher passwordHasher,
      BirthDate birthDate,
      Gender gender,
      Clock clock) {
    List<String> passwordViolations = password.violations(email);
    if (!passwordViolations.isEmpty()) {
      throw new InvalidFieldException("password", passwordViolations);
    }
    Instant now = clock.instant();
    return new User(
        UUID.randomUUID(), name, email, passwordHasher.hash(password), birthDate, gender, now, now);
  }

  public User rename(FullName newName, Clock clock) {
    return this;
  }

  @Override
  public String toString() {
    return "User[****]";
  }
}
