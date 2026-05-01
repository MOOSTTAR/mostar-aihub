package com.mostar.langchain4jtest.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mostar.langchain4jtest.entity.dto.LoginRequest;
import com.mostar.langchain4jtest.entity.dto.LoginResponse;
import com.mostar.langchain4jtest.entity.po.User;
import com.mostar.langchain4jtest.service.AuthService;
import com.mostar.langchain4jtest.service.IUserService;
import com.mostar.langchain4jtest.utils.JwtUtil;

import cn.hutool.crypto.digest.DigestUtil;
import jakarta.annotation.Resource;

import static com.mostar.langchain4jtest.constants.RedisConstants.TOKEN_PREFIX;

@Service
public class AuthServiceImpl implements AuthService {

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private JwtUtil jwtUtil;

	@Resource
	private PasswordEncoder passwordEncoder;

	@Resource
	private IUserService userService; // 假设已有用户服务

	@Value("${jwt.default-expiration}")
	private long defaultExpiration;

	@Value("${jwt.remember-me-expiration}")
	private long rememberMeExpiration;

	/**
	 * 用户登录
	 */
	@SuppressWarnings("null")
	public LoginResponse login(LoginRequest request) {
		// 1. 验证用户名密码
		User user = userService.getByUsername(request.getUsername());
		if (user == null) {
			throw new RuntimeException("用户名不存在");
		}
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new RuntimeException("密码错误");
		}

		// 2. 判断是否记住我
		boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());
		long expiration = rememberMe ? rememberMeExpiration : defaultExpiration;

		// 3. 生成 JWT（携带 rememberMe 标志）
		String token = jwtUtil.generateToken(user.getId(), user.getUsername(), rememberMe, expiration);

		// 4. 存储到 Redis，Key 为 token 的 MD5 值，过期时间设为 token 有效期的 2 倍（提供续期缓冲）
		String redisKey = TOKEN_PREFIX + DigestUtil.md5Hex(token);
		stringRedisTemplate.opsForValue().set(redisKey, token, expiration * 2, TimeUnit.MILLISECONDS);

		// 5. 返回响应
		LoginResponse response = new LoginResponse();
		response.setToken(token);
		response.setUserId(user.getId());
		response.setUsername(user.getUsername());
		response.setExpiresIn(expiration / 1000);
		return response;
	}

	/**
	 * 验证 token 并自动续期（如果 JWT 过期但 Redis 未过期） 逻辑： 1. JWT 未过期 → 放行 2. JWT 过期但 Redis 未过期
	 * → 续期，返回新 token 3. JWT 过期且 Redis 过期 → 返回 null，需要重新登录
	 *
	 * @param token
	 *            原始 JWT
	 * @return token（可能是新 token），如果失效则返回 null
	 */
	@SuppressWarnings("null")
	public String validateAndRenewIfNeeded(String token) {
		// 1. 校验 JWT 签名
		if (!jwtUtil.verify(token)) {
			return null;
		}

		// 2. 检查 Redis 中是否存在
		String redisKey = TOKEN_PREFIX + DigestUtil.md5Hex(token);
		Boolean exists = stringRedisTemplate.hasKey(redisKey);
		if (exists == null || !exists) {
			return null; // Redis 已过期，需要重新登录
		}

		// 3. 检查 JWT 是否过期
		if (jwtUtil.isExpired(token)) {
			// JWT 已过期但 Redis 中还有，自动续期
			Long userId = jwtUtil.getUserIdFromToken(token);
			String username = jwtUtil.getUsernameFromToken(token);
			boolean rememberMe = jwtUtil.getRememberMeFromToken(token);
			long expiration = rememberMe ? rememberMeExpiration : defaultExpiration;

			String newToken = jwtUtil.generateToken(userId, username, rememberMe, expiration);

			// 更新 Redis：删除旧 Key，存储新 Key
			stringRedisTemplate.delete(redisKey);
			String newRedisKey = TOKEN_PREFIX + DigestUtil.md5Hex(newToken);
			stringRedisTemplate.opsForValue().set(newRedisKey, newToken, expiration * 2, TimeUnit.MILLISECONDS);
			return newToken;
		}

		// JWT 未过期，直接放行
		return token;
	}

	/**
	 * 主动续期 token
	 *
	 * @param token
	 *            原始 JWT
	 * @return 续期后的新 token，如果 token 已失效则返回 null
	 */
	@SuppressWarnings("null")
	public String renewToken(String token) {
		// 1. 校验 JWT 签名
		if (!jwtUtil.verify(token)) {
			return null;
		}

		// 2. 检查 Redis 中是否存在
		String redisKey = TOKEN_PREFIX + DigestUtil.md5Hex(token);
		Boolean exists = stringRedisTemplate.hasKey(redisKey);
		if (exists == null || !exists) {
			return null;
		}

		// 3. 重新生成 token（主动续期，无论剩余时间多少都续）
		Long userId = jwtUtil.getUserIdFromToken(token);
		String username = jwtUtil.getUsernameFromToken(token);
		boolean rememberMe = jwtUtil.getRememberMeFromToken(token);
		long expiration = rememberMe ? rememberMeExpiration : defaultExpiration;

		String newToken = jwtUtil.generateToken(userId, username, rememberMe, expiration);

		// 更新 Redis
		stringRedisTemplate.delete(redisKey);
		String newRedisKey = TOKEN_PREFIX + DigestUtil.md5Hex(newToken);
		stringRedisTemplate.opsForValue().set(newRedisKey, newToken, expiration * 2, TimeUnit.MILLISECONDS);
		return newToken;
	}

	/**
	 * 登出：删除 Redis 中的 token
	 */
	public void logout(String token) {
		if (token != null && !token.isEmpty()) {
			String redisKey = TOKEN_PREFIX + DigestUtil.md5Hex(token);
			stringRedisTemplate.delete(redisKey);
		}
	}

	// 以下方法为 JwtAuthenticationFilter 提供，直接委托给 JwtUtil
	public String getUsernameFromToken(String token) {
		return jwtUtil.getUsernameFromToken(token);
	}
}
