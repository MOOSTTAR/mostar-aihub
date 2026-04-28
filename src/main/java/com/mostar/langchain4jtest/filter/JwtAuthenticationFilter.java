package com.mostar.langchain4jtest.filter;

import com.mostar.langchain4jtest.service.impl.AuthServiceImpl;
import com.mostar.langchain4jtest.utils.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Resource
    private AuthServiceImpl authService;

    @Resource
    private JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 获取 token
        String header = request.getHeader(TOKEN_HEADER);
        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(TOKEN_PREFIX.length());

        // 验证 token 并自动续期（如果 JWT 过期但 Redis 未过期）
        String validatedToken = authService.validateAndRenewIfNeeded(token);

        if (validatedToken == null) {
            // token 已失效，返回 401
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":401,\"message\":\"Token已失效，请重新登录\"}");
            }
            return;
        }

        // 如果 token 被续期了，将新 token 放入响应头返回给前端
        if (!validatedToken.equals(token)) {
            response.setHeader("X-New-Token", validatedToken);
        }

        // 从 token 中获取用户信息并设置到 SecurityContext
        String username = jwtUtil.getUsernameFromToken(validatedToken);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = User.builder()
                    .username(username)
                    .password("")
                    .authorities(new ArrayList<>())
                    .build();
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}