package com.mostar.langchain4jtest.service;

import com.mostar.langchain4jtest.entity.dto.LoginRequest;
import com.mostar.langchain4jtest.entity.dto.LoginResponse;
import com.mostar.langchain4jtest.entity.po.User;
import com.mostar.langchain4jtest.utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public interface AuthService {

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request);

    /**
     * 验证 token 并自动续期（如果 JWT 过期但 Redis 未过期）
     * @return token（可能是新 token），如果失效则返回 null
     */
    public String validateAndRenewIfNeeded(String token);

    /**
     * 主动续期 token
     * @param token 原始 JWT
     * @return 续期后的新 token，如果 token 已失效则返回 null
     */
    public String renewToken(String token);

    /**
     * 登出
     */
    public void logout(String token) ;
}