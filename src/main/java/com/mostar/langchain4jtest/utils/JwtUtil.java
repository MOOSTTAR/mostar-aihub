package com.mostar.langchain4jtest.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	private byte[] getSecretBytes() {
		return secret.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * 生成 JWT Token
	 *
	 * @param userId
	 *            用户ID
	 * @param username
	 *            用户名
	 * @param expiration
	 *            有效期（毫秒）
	 * @return JWT Token 字符串
	 */
	public String generateToken(Long userId, String username, boolean rememberMe, long expiration) {
		Date now = new Date();
		Date expireDate = new Date(now.getTime() + expiration);

		Map<String, Object> payload = new HashMap<>();
		payload.put("userId", userId);
		payload.put("username", username);
		payload.put("rememberMe", rememberMe); // 新增标志
		payload.put("iat", System.currentTimeMillis() / 1000); // 签发时间
		payload.put("exp", (System.currentTimeMillis() + expiration) / 1000); // 过期时间

		return JWT.create().addPayloads(payload).setKey(getSecretBytes()).sign();
	}

	/**
	 * 验证 JWT Token 签名
	 */
	public boolean verify(String token) {
		return JWTUtil.verify(token, getSecretBytes());
	}

	/**
	 * 验证 JWT Token 是否过期
	 */
	public boolean isExpired(String token) {
		try {
			JWTValidator.of(token).validateDate(new Date());
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * 从 Token 中获取用户ID
	 */
	public Long getUserIdFromToken(String token) {
		JWT jwt = JWTUtil.parseToken(token);
		Number userIdNum = (Number) jwt.getPayload("userId");
		return userIdNum.longValue();
	}

	/**
	 * 从 Token 中获取用户名
	 */
	public String getUsernameFromToken(String token) {
		JWT jwt = JWTUtil.parseToken(token);
		return (String) jwt.getPayload("username");
	}

	/**
	 * 获取 Token 的过期时间
	 */
	public Date getExpirationDate(String token) {
		JWT jwt = JWTUtil.parseToken(token);
		Number expSeconds = (Number) jwt.getPayload("exp");
		return new Date(expSeconds.longValue() * 1000);
	}

	public Boolean getRememberMeFromToken(String token) {
		JWT jwt = JWTUtil.parseToken(token);
		return (Boolean) jwt.getPayload("rememberMe");
	}
}
