package com.nongzhushou.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RoleSwitchRequest {

    @NotBlank
    @Pattern(regexp = "(?i)farmer|owner|admin", message = "role must be farmer, owner or admin")
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
