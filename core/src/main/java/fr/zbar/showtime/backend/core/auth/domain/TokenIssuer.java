package fr.zbar.showtime.backend.core.auth.domain;

import java.util.UUID;

public interface TokenIssuer {

  String issue(UUID userId);
}
