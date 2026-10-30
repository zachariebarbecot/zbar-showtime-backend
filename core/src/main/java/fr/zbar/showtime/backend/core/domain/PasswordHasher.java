package fr.zbar.showtime.backend.core.domain;

public interface PasswordHasher {

  PasswordHash hash(RawPassword password);
}
