package com.mostar.langchain4jtest.entity.dto;

import lombok.Data;

@Data
public class LoginResponse {
	private String token;
	private Long userId;
	private String username;
	private Long expiresIn; // token 有效期（秒）
}