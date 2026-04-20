package com.mostar.langchain4jtest.controller;

import com.mostar.langchain4jtest.entity.dto.ChatMessageDTO;
import com.mostar.langchain4jtest.service.ChatHistoryService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat/history")
@Slf4j
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @GetMapping("/{memoryId}")
    public List<ChatMessageDTO> getChatHistory(@PathVariable String memoryId, HttpServletRequest request) {
        return chatHistoryService.getChatHistory(memoryId, request);
    }
}
