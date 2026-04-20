package com.mostar.langchain4jtest.entity.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private Boolean rememberMe;  // true: 记住我（7天），false/不传: 30分钟
}