package com.mostar.langchain4jtest.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mostar.langchain4jtest.entity.dto.UserDTO;
import com.mostar.langchain4jtest.entity.vo.UserVO;
import com.mostar.langchain4jtest.service.IUserService;
import com.mostar.langchain4jtest.utils.JwtUtil;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户登录表 前端控制器
 * </p>
 *
 * @author MOstAr
 * @since 2026-04-05
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Resource
	private IUserService userService;

	@Resource
	private JwtUtil jwtUtil;

	/**
	 * 获取当前用户信息
	 */
	@GetMapping("/info")
	public Map<String, Object> getUserInfo(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<>();
		try {
			Long userId = getUserIdFromRequest(request);
			if (userId == null) {
				result.put("success", false);
				result.put("message", "未登录或 token 无效");
				return result;
			}

			UserVO userInfo = userService.getUserInfo(userId);
			if (userInfo == null) {
				result.put("success", false);
				result.put("message", "用户不存在");
				return result;
			}

			result.put("success", true);
			result.put("data", userInfo);
		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "获取用户信息失败：" + e.getMessage());
		}
		return result;
	}

	/**
	 * 更新用户信息
	 * 注意：用户名不允许修改
	 */
	@PutMapping("/info")
	public Map<String, Object> updateUserInfo(@RequestBody UserDTO userDTO, HttpServletRequest request) {
		Map<String, Object> result = new HashMap<>();
		try {
			Long userId = getUserIdFromRequest(request);
			if (userId == null) {
				result.put("success", false);
				result.put("message", "未登录或 token 无效");
				return result;
			}

			// 清除用户名（不允许修改）
			userDTO.setUsername(null);

			boolean success = userService.updateUserInfo(userId, userDTO);
			result.put("success", success);
			if (success) {
				result.put("message", "更新成功");
				// 返回更新后的用户信息
				result.put("data", userService.getUserInfo(userId));
			} else {
				result.put("message", "更新失败");
			}
		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "更新用户信息失败：" + e.getMessage());
		}
		return result;
	}

	/**
	 * 更新头像（预留接口）
	 */
	@PostMapping("/avatar")
	public Map<String, Object> updateAvatar(@RequestBody Map<String, String> body, HttpServletRequest request) {
		Map<String, Object> result = new HashMap<>();
		result.put("success", false);
		result.put("message", "头像修改功能尚未开发");
		return result;
	}

	/**
	 * 从请求中获取用户 ID
	 */
	private Long getUserIdFromRequest(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return null;
		}

		String token = authHeader.substring(7);
		try {
			return jwtUtil.getUserIdFromToken(token);
		} catch (Exception e) {
			return null;
		}
	}
}
