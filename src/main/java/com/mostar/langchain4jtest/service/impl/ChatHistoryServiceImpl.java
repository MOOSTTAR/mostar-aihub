package com.mostar.langchain4jtest.service.impl;

import com.mostar.langchain4jtest.entity.dto.ChatMessageDTO;
import com.mostar.langchain4jtest.repository.RedisChatMemoryStore;
import com.mostar.langchain4jtest.service.ChatHistoryService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.SystemMessage;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Override
    public List<ChatMessageDTO> getChatHistory(String memoryId, HttpServletRequest request) {
        List<ChatMessage> messages = redisChatMemoryStore.getMessages(memoryId);

        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        return messages.stream()
                .filter(msg -> !(msg instanceof SystemMessage))  // 过滤掉系统消息
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ChatMessageDTO convertToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();

        if (message instanceof UserMessage) {
            dto.setRole("user");
            dto.setContent(((UserMessage) message).singleText());
        } else if (message instanceof AiMessage) {
            dto.setRole("assistant");
            dto.setContent(((AiMessage) message).text());
        } else {
            dto.setRole("unknown");
            dto.setContent(message.toString());
        }

        return dto;
    }
}
