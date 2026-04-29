package com.nongzhushou.v3user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class V3SelectRoleRequest {

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "FARMER|OPERATOR", message = "角色只能是 FARMER 或 OPERATOR")
    private String role;

    private String realName;       // 可选实名
    private String homeLocation;   // 农户填写家庭位置

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getHomeLocation() { return homeLocation; }
    public void setHomeLocation(String homeLocation) { this.homeLocation = homeLocation; }
}
