package com.mostar.langchain4jtest.service;

import com.mostar.langchain4jtest.entity.dto.ChatSessionDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ChatSessionService {

    /**
     * 获取当前用户的所有会话列表
     */
    List<ChatSessionDTO> getUserSessions(HttpServletRequest request);

    /**
     * 删除指定会话
     */
    void deleteSession(String memoryId, HttpServletRequest request);

    /**
     * 创建新会话记录
     */
    void createSession(String memoryId, String firstMessage, Long userId);

    /**
     * 更新会话标题（如果还没有标题）
     */
    void updateSessionTitle(String memoryId, String title, Long userId);
}
