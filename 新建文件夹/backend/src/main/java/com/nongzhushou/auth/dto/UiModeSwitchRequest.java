package com.nongzhushou.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UiModeSwitchRequest {

    @NotBlank
    @Pattern(regexp = "(?i)normal|elder", message = "uiMode must be normal or elder")
    private String uiMode;

    public String getUiMode() {
        return uiMode;
    }

    public void setUiMode(String uiMode) {
        this.uiMode = uiMode;
    }
}
