package com.mostar.langchain4jtest.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.mostar.langchain4jtest.entity.dto.LoginRequest;
import com.mostar.langchain4jtest.entity.dto.LoginResponse;
import com.mostar.langchain4jtest.entity.po.User;
import com.mostar.langchain4jtest.service.AuthService;
import com.mostar.langchain4jtest.service.IUserService;
import com.mostar.langchain4jtest.utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.mostar.langchain4jtest.constants.RedisConstants.TOKEN_PREFIX;

@Service
public class AuthServiceImpl implements AuthService{

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private IUserService userService;  // 假设已有用户服务

    @Value("${jwt.default-expiration}")
    private long defaultExpiration;

    @Value("${jwt.remember-me-expiration}")
    private long rememberMeExpiration;

    @Value("${jwt.renew-threshold}")
    private long renewThreshold;

    /**
     * 用户登录
     */
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
     * 检查并续期
     * @param token 原始 JWT
     * @return 续期后的新 token，如果无需续期则返回原 token，如果 token 已失效则返回 null
     */
    public String renewTokenIfNeeded(String token) {
        // 1. 校验 JWT 签名
        if (!jwtUtil.verify(token)) {
            return null;
        }

        // 2. 检查 Redis 中是否存在（Key 使用 MD5）
        String redisKey = TOKEN_PREFIX + DigestUtil.md5Hex(token);
        Boolean exists = stringRedisTemplate.hasKey(redisKey);
        if (exists == null || !exists) {
            return null;  // Redis 中已过期，需要重新登录
        }

        // 3. 检查 JWT 是否过期
        if (jwtUtil.isExpired(token)) {
            // JWT 已过期但 Redis 中还有（缓冲期内），重新生成 token
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

        // 4. 检查是否需要续期（剩余有效期小于阈值）
        Date expDate = jwtUtil.getExpirationDate(token);
        long remaining = expDate.getTime() - System.currentTimeMillis();
        if (remaining <= renewThreshold) {
            // 续期：重新生成 token，有效期根据 rememberMe 标志决定
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

        // 无需续期
        return token;
    }

    /**
     * 主动续期 token
     * @param token 原始 JWT
     * @return 续期后的新 token，如果 token 已失效则返回 null
     */
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