package fr.zbar.showtime.backend.core.domain;

import java.util.UUID;

public interface TokenIssuer {

  String issue(UUID userId);
}
