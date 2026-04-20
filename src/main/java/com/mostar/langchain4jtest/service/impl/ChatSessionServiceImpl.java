package com.mostar.langchain4jtest.service.impl;

import com.mostar.langchain4jtest.entity.dto.ChatSessionDTO;
import com.mostar.langchain4jtest.service.ChatSessionService;
import com.mostar.langchain4jtest.utils.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtUtil jwtUtil;

    private static final String SESSION_KEY_PREFIX = "chat:sessions:";
    private static final String SESSION_INFO_PREFIX = "chat:session:info:";

    @Override
    public List<ChatSessionDTO> getUserSessions(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return List.of();
        }

        String sessionKey = SESSION_KEY_PREFIX + userId;
        Set<String> memoryIds = stringRedisTemplate.opsForSet().members(sessionKey);

        if (memoryIds == null || memoryIds.isEmpty()) {
            return List.of();
        }

        return memoryIds.stream()
                .map(memoryId -> {
                    String infoKey = SESSION_INFO_PREFIX + memoryId;
                    Map<Object, Object> info = stringRedisTemplate.opsForHash().entries(infoKey);

                    ChatSessionDTO dto = new ChatSessionDTO();
                    dto.setMemoryId(memoryId);
                    dto.setTitle((String) info.getOrDefault("title", "新会话"));
                    Object createTime = info.get("createTime");
                    if (createTime != null) {
                        dto.setCreateTime(Long.parseLong(createTime.toString()));
                    } else {
                        dto.setCreateTime(System.currentTimeMillis());
                    }
                    return dto;
                })
                .sorted(Comparator.comparingLong(ChatSessionDTO::getCreateTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSession(String memoryId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new RuntimeException("未登录");
        }

        String sessionKey = SESSION_KEY_PREFIX + userId;
        String infoKey = SESSION_INFO_PREFIX + memoryId;
        String memoryKey = memoryId;

        // 从用户会话列表中移除
        stringRedisTemplate.opsForSet().remove(sessionKey, memoryId);
        // 删除会话信息
        stringRedisTemplate.delete(infoKey);
        // 删除会话内容
        stringRedisTemplate.delete(memoryKey);

        log.info("用户 {} 删除会话 {}", userId, memoryId);
    }

    @Override
    public void createSession(String memoryId, String firstMessage, Long userId) {
        if (userId == null) {
            return;
        }

        String sessionKey = SESSION_KEY_PREFIX + userId;
        String infoKey = SESSION_INFO_PREFIX + memoryId;

        // 添加到用户会话列表
        stringRedisTemplate.opsForSet().add(sessionKey, memoryId);

        // 保存会话信息
        Map<String, String> info = new HashMap<>();
        info.put("title", truncateMessage(firstMessage, 20));
        info.put("createTime", String.valueOf(System.currentTimeMillis()));
        info.put("userId", String.valueOf(userId));

        stringRedisTemplate.opsForHash().putAll(infoKey, info);

        log.info("用户 {} 创建新会话 {}", userId, memoryId);
    }

    @Override
    public void updateSessionTitle(String memoryId, String title, Long userId) {
        if (userId == null || memoryId == null) {
            return;
        }

        String infoKey = SESSION_INFO_PREFIX + memoryId;
        stringRedisTemplate.opsForHash().put(infoKey, "title", truncateMessage(title, 20));
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
            log.error("解析token失败", e);
            return null;
        }
    }

    private String truncateMessage(String message, int maxLength) {
        if (message == null || message.isEmpty()) {
            return "新会话";
        }
        // 去除换行符和多余空格
        String cleaned = message.replaceAll("\\s+", " ").trim();
        if (cleaned.length() > maxLength) {
            return cleaned.substring(0, maxLength) + "...";
        }
        return cleaned;
    }
}
