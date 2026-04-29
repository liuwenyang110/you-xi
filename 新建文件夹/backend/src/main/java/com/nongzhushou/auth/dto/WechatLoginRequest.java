package com.nongzhushou.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 微信公众号/小程序登录请求
 */
public class WechatLoginRequest {

    /**
     * wx.login() 获取的临时登录凭证
     */
    @NotBlank(message = "wx login code cannot be empty")
    private String code;

    /**
     * 微信头像 URL
     */
    private String avatarUrl;

    /**
     * 微信昵称
     */
    private String nickname;

    /**
     * 用户所属的应用场景 (MINI_PROGRAM, H5, APP)
     */
    private String source = "MINI_PROGRAM";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
