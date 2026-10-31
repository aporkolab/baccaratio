package com.prokey.baccaratio.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registration data. The password rules match the frontend's register form.
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "[A-Za-z0-9_.-]+", message = "may only contain letters, digits, '.', '_' and '-'")
        String username,
        @NotBlank @Size(min = 6, max = 72) String password // BCrypt only uses the first 72 bytes
) {}
