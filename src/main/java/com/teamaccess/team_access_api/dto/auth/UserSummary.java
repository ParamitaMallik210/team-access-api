package com.teamaccess.team_access_api.dto.auth;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String email,
        String fullName) {
}