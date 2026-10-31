package com.prokey.baccaratio.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Login credentials.
 */
public record AuthRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(max = 72) String password // BCrypt only uses the first 72 bytes
) {}
