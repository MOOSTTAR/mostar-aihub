package com.mostar.langchain4jtest.service;

import com.mostar.langchain4jtest.entity.dto.ChatMessageDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ChatHistoryService {

    /**
     * 获取指定会话的历史消息
     */
    List<ChatMessageDTO> getChatHistory(String memoryId, HttpServletRequest request);
}
