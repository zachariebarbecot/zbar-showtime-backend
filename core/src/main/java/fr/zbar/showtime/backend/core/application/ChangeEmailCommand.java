package fr.zbar.showtime.backend.core.application;

import java.util.UUID;

public record ChangeEmailCommand(UUID userId, String currentPassword, String newEmail) {}
