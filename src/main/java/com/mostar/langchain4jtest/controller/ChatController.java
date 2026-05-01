package com.mostar.langchain4jtest.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mostar.langchain4jtest.aiservice.ConsultantService;
import com.mostar.langchain4jtest.entity.Result;
import com.mostar.langchain4jtest.repository.RedisChatMemoryStore;
import com.mostar.langchain4jtest.service.ChatSessionService;
import com.mostar.langchain4jtest.utils.JwtUtil;

import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {
    @Resource
    private OpenAiChatModel openAiChatModel;

    @Resource
    private ConsultantService consultantService;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestParam String memoryId, @RequestParam String message, HttpServletRequest request) {
        log.info("Chat request - memoryId: {}, message: {}", memoryId, message);

        // 获取用户ID并记录会话
        Long userId = getUserIdFromRequest(request);
        if (userId != null) {
            chatSessionService.createSession(memoryId, message, userId);
        }

        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        Flux<String> flux = consultantService.chat(memoryId, message);

        flux.subscribe(
            content -> {
                try {
                    emitter.send(SseEmitter.event()
                        .data(content)
                        .build());
                } catch (IOException e) {
                    log.error("Error sending SSE event", e);
                    emitter.completeWithError(e);
                }
            },
            error -> {
                log.error("Chat error: ", error);
                emitter.completeWithError(error);
            },
            () -> {
                log.info("Chat completed");
                emitter.complete();
            }
        );

        return emitter;
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

    /**
     * 清空当前对话记录和 AI 记忆
     */
    @PostMapping(value = "/clear", produces = "application/json")
    public Result<String> clearChat(@RequestParam String memoryId, HttpServletRequest request) {
        log.info("Clear chat memory - memoryId: {}", memoryId);

        // 清空 Redis 中的 AI 记忆
        redisChatMemoryStore.deleteMessages(memoryId);

        // 重置会话标题为"新对话"
        Long userId = getUserIdFromRequest(request);
        if (userId != null) {
            chatSessionService.updateSessionTitle(memoryId, "新对话", userId);
        }

        return Result.ok("清空成功");
    }

    /**
     * 批量删除会话
     */
    @PostMapping("/sessions/batch-delete")
    public Result<String> batchDeleteSessions(@RequestBody java.util.List<String> memoryIds, HttpServletRequest request) {
        log.info("Batch delete sessions - memoryIds: {}", memoryIds);

        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("未授权");
        }

        chatSessionService.deleteSessions(memoryIds, userId);
        return Result.ok("批量删除成功");
    }
}
