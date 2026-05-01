package com.mostar.langchain4jtest.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mostar.langchain4jtest.entity.dto.ChatMessageDTO;
import com.mostar.langchain4jtest.repository.RedisChatMemoryStore;
import com.mostar.langchain4jtest.service.ChatHistoryService;
import com.mostar.langchain4jtest.utils.XssFilter;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

	@Resource
	private RedisChatMemoryStore redisChatMemoryStore;

	@Override
	public List<ChatMessageDTO> getChatHistory(String memoryId, HttpServletRequest request) {
		// 1. 从历史记录中获取（之前 /clear 保存的对话）
		List<ChatMessage> historyMessages = redisChatMemoryStore.getHistory(memoryId);

		// 2. 从 AI 记忆中获取（当前对话，可能包含 /clear 后的新对话）
		List<ChatMessage> currentMessages = redisChatMemoryStore.getMessages(memoryId);

		// 3. 合并：history + current
		List<ChatMessage> allMessages = new java.util.ArrayList<>(historyMessages);
		for (ChatMessage msg : currentMessages) {
			// 简单去重：如果消息内容已存在则跳过
			boolean exists = allMessages.stream()
					.anyMatch(m -> m.toString().equals(msg.toString()));
			if (!exists) {
				allMessages.add(msg);
			}
		}

		if (allMessages.isEmpty()) {
			return List.of();
		}

		return allMessages.stream().filter(msg -> !(msg instanceof SystemMessage)) // 过滤掉系统消息
				.map(this::convertToDTO).collect(Collectors.toList());
	}

	private ChatMessageDTO convertToDTO(ChatMessage message) {
		ChatMessageDTO dto = new ChatMessageDTO();

		if (message instanceof UserMessage) {
			dto.setRole("user");
			dto.setContent(XssFilter.filter(((UserMessage) message).singleText()));
		} else if (message instanceof AiMessage) {
			dto.setRole("assistant");
			dto.setContent(XssFilter.filter(((AiMessage) message).text()));
		} else {
			dto.setRole("unknown");
			dto.setContent(XssFilter.filter(message.toString()));
		}

		return dto;
	}
}
