package com.mostar.langchain4jtest.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.mostar.langchain4jtest.entity.dto.ChatSessionDTO;
import com.mostar.langchain4jtest.service.ChatSessionService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/chat/sessions")
@Slf4j
public class ChatSessionController {

	@Resource
	private ChatSessionService chatSessionService;

	@GetMapping
	public List<ChatSessionDTO> getUserSessions(HttpServletRequest request) {
		return chatSessionService.getUserSessions(request);
	}

	@DeleteMapping("/{memoryId}")
	public void deleteSession(@PathVariable String memoryId, HttpServletRequest request) {
		chatSessionService.deleteSession(memoryId, request);
	}
}
