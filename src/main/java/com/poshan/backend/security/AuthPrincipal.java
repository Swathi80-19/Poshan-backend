package com.poshan.backend.security;

import com.poshan.backend.enums.Role;

public record AuthPrincipal(
    Long id,
    Role role,
    String email
) {
}
