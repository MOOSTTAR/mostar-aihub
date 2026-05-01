package com.mostar.langchain4jtest.service;

import com.mostar.langchain4jtest.entity.dto.LoginRequest;
import com.mostar.langchain4jtest.entity.dto.LoginResponse;

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
