package fr.zbar.showtime.backend.core.user.application;

import java.util.UUID;

public record UpdateProfileCommand(UUID userId, String lastName, String firstName) {}
