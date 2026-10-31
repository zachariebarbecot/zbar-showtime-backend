package fr.zbar.showtime.backend.core.application;

import java.util.UUID;

public record UpdateProfileCommand(UUID userId, String lastName, String firstName) {}
