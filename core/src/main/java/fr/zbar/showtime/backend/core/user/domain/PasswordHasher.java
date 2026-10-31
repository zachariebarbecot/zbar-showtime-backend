package fr.zbar.showtime.backend.core.user.domain;

public interface PasswordHasher {

  PasswordHash hash(RawPassword password);
}
