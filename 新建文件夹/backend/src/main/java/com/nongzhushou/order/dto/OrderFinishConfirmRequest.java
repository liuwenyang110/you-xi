package com.nongzhushou.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OrderFinishConfirmRequest {

    @NotBlank
    @Pattern(regexp = "(?i)owner|farmer", message = "actorRole must be owner or farmer")
    private String actorRole;

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }
}
