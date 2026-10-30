package fr.zbar.showtime.backend.core.domain;

public interface PasswordVerifier {

  boolean matches(RawPassword password, PasswordHash hash);
}
