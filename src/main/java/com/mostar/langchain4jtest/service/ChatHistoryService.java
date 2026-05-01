package com.mostar.langchain4jtest.service;

import java.util.List;

import com.mostar.langchain4jtest.entity.dto.ChatMessageDTO;

import jakarta.servlet.http.HttpServletRequest;

public interface ChatHistoryService {

	/**
	 * 获取指定会话的历史消息
	 */
	List<ChatMessageDTO> getChatHistory(String memoryId, HttpServletRequest request);
}
