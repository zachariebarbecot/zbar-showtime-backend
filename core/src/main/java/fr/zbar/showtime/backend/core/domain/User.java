package fr.zbar.showtime.backend.core.domain;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class User extends AggregateRoot<UUID> {

  private final FullName name;
  private final Email email;
  private final PasswordHash passwordHash;
  private final BirthDate birthDate;
  private final Gender gender;
  private final Instant createdAt;
  private final Instant updatedAt;

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
    Instant now = clock.instant();
    return new User(
        UUID.randomUUID(), name, email, passwordHasher.hash(password), birthDate, gender, now, now);
  }

  public FullName name() {
    return name;
  }

  public Email email() {
    return email;
  }

  public PasswordHash passwordHash() {
    return passwordHash;
  }

  public BirthDate birthDate() {
    return birthDate;
  }

  public Gender gender() {
    return gender;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }
}
