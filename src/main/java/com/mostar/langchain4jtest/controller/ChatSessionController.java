package com.mostar.langchain4jtest.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.mostar.langchain4jtest.entity.dto.ChatSessionDTO;
import com.mostar.langchain4jtest.service.ChatSessionService;
import com.mostar.langchain4jtest.utils.JwtUtil;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/chat/sessions")
public class ChatSessionController {

	@Resource
	private ChatSessionService chatSessionService;

	@Resource
	private JwtUtil jwtUtil;

	@GetMapping
	public List<ChatSessionDTO> getUserSessions(HttpServletRequest request) {
		return chatSessionService.getUserSessions(request);
	}

	@DeleteMapping("/{memoryId}")
	public void deleteSession(@PathVariable String memoryId, HttpServletRequest request) {
		chatSessionService.deleteSession(memoryId, request);
	}

	/**
	 * 更新会话置顶状态
	 */
	@PostMapping("/{memoryId}/pin")
	public void updatePinStatus(@PathVariable String memoryId,
	                             @RequestBody Map<String, Boolean> body,
	                             HttpServletRequest request) {
		Long userId = getUserIdFromRequest(request);
		if (userId == null) {
			throw new RuntimeException("未登录");
		}
		chatSessionService.updateSessionPinStatus(memoryId, body.get("isPinned"), userId);
	}

	/**
	 * 更新会话标题
	 */
	@PutMapping("/{memoryId}/title")
	public void updateTitle(@PathVariable String memoryId,
	                        @RequestBody Map<String, String> body,
	                        HttpServletRequest request) {
		Long userId = getUserIdFromRequest(request);
		if (userId == null) {
			throw new RuntimeException("未登录");
		}
		chatSessionService.updateSessionTitleById(memoryId, body.get("title"), userId);
	}

	private Long getUserIdFromRequest(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return null;
		}

		String token = authHeader.substring(7);
		try {
			return jwtUtil.getUserIdFromToken(token);
		} catch (Exception e) {
			log.error("解析 token 失败", e);
			return null;
		}
	}
}
