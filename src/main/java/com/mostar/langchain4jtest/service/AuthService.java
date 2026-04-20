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
     * 检查并续期
     * @return 续期后的新 token，如果无需续期则返回原 token，如果 token 已失效则返回 null
     */
    public String renewTokenIfNeeded(String token);

    /**
     * 登出
     */
    public void logout(String token) ;
}