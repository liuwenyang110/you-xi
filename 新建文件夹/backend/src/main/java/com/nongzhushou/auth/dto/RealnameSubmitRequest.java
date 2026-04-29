package com.nongzhushou.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RealnameSubmitRequest {

    @NotBlank
    @Size(min = 2, max = 20, message = "realName length must be between 2 and 20")
    private String realName;

    @NotBlank
    @Pattern(regexp = "^[0-9Xx]{15,18}$", message = "idCardNo format is invalid")
    private String idCardNo;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }
}
