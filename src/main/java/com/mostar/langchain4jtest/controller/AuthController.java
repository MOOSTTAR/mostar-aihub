package com.mostar.langchain4jtest.controller;

import com.mostar.langchain4jtest.entity.Result;
import com.mostar.langchain4jtest.entity.dto.LoginRequest;
import com.mostar.langchain4jtest.entity.dto.LoginResponse;
import com.mostar.langchain4jtest.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/test")
    public String test() {
        return "Login success! Token is valid.";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
    }

    /**
     * 主动续期 token
     */
    @PostMapping("/renew")
    public Result<String> renewToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未授权");
        }
        String token = authHeader.substring(7);
        String newToken = authService.renewToken(token);
        if (newToken == null) {
            return Result.error("Token 已失效，请重新登录");
        }
        return Result.ok(newToken);
    }
}