package com.mostar.langchain4jtest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mostar.langchain4jtest.filter.JwtAuthenticationFilter;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Resource
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()) // JWT 无状态，禁用 CSRF
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 无状态会话
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint((request, response, authException) -> {
							// 自定义 401 处理，避免与流式响应冲突
							if (!response.isCommitted()) {
								response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
								response.setContentType("application/json;charset=UTF-8");
								response.getWriter().write("{\"code\":401,\"message\":\"未登录或 Token 已过期\"}");
							}
						})
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							// 自定义 403 处理
							if (!response.isCommitted()) {
								response.setStatus(HttpServletResponse.SC_FORBIDDEN);
								response.setContentType("application/json;charset=UTF-8");
								response.getWriter().write("{\"code\":403,\"message\":\"拒绝访问\"}");
							}
						}))
				.authorizeHttpRequests(auth -> auth
						// 放行登录注册
						.requestMatchers("/auth/login", "/auth/register").permitAll()
						// 放行 Swagger / Knife4j 相关路径
						.requestMatchers("/doc.html", "/swagger-resources/**", "/swagger-ui/**", "/v3/api-docs/**",
								"/webjars/**")
						.permitAll()
						// 放行错误页面
						.requestMatchers("/error").permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
