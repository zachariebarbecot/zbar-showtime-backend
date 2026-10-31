package fr.zbar.showtime.backend.core.user.domain;

public interface PasswordVerifier {

  boolean matches(RawPassword password, PasswordHash hash);
}
