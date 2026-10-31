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
    requireCompliant(password, email, "password");
    Instant now = clock.instant();
    return new User(
        UUID.randomUUID(), name, email, passwordHasher.hash(password), birthDate, gender, now, now);
  }

  public User rename(FullName newName, Clock clock) {
    if (name.equals(newName)) {
      return this;
    }
    return new User(
        id(), newName, email, passwordHash, birthDate, gender, createdAt, clock.instant());
  }

  public User changeEmail(Email newEmail, Clock clock) {
    if (email.equals(newEmail)) {
      return this;
    }
    return new User(
        id(), name, newEmail, passwordHash, birthDate, gender, createdAt, clock.instant());
  }

  public User changePassword(RawPassword newPassword, PasswordHasher passwordHasher, Clock clock) {
    requireCompliant(newPassword, email, "newPassword");
    return new User(
        id(),
        name,
        email,
        passwordHasher.hash(newPassword),
        birthDate,
        gender,
        createdAt,
        clock.instant());
  }

  public void requirePassword(RawPassword password, PasswordVerifier passwordVerifier) {}

  public User changePassword(
      RawPassword currentPassword,
      RawPassword newPassword,
      PasswordVerifier passwordVerifier,
      PasswordHasher passwordHasher,
      Clock clock) {
    return this;
  }

  private static void requireCompliant(RawPassword password, Email email, String field) {
    List<String> violations = password.violations(email);
    if (!violations.isEmpty()) {
      throw new InvalidFieldException(field, violations);
    }
  }

  @Override
  public String toString() {
    return "User[****]";
  }
}
