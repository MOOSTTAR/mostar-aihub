package com.mostar.langchain4jtest.repository;

import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;

@Repository
public class RedisChatMemoryStore implements ChatMemoryStore {
	@Resource
	private StringRedisTemplate stringRedisTemplate;

	private static final String CONTENT_PREFIX = "chat:session:content:";
	private static final String HISTORY_PREFIX = "chat:session:history:";

	@Override
	public List<ChatMessage> getMessages(Object memoryId) {
		String key = CONTENT_PREFIX + memoryId.toString();
		String json = stringRedisTemplate.opsForValue().get(key);
		return ChatMessageDeserializer.messagesFromJson(json);
	}

	@Override
	@SuppressWarnings("null")
	public void updateMessages(Object memoryId, List<ChatMessage> list) {
		String key = CONTENT_PREFIX + memoryId.toString();
		String json = ChatMessageSerializer.messagesToJson(list);
		stringRedisTemplate.opsForValue().set(key, json);

		// 同时保存到历史记录（用于前端显示）
		saveToHistory(memoryId.toString(), list);
	}

	/**
	 * 保存消息到历史记录（前端显示用）
	 */
	public void saveToHistory(String memoryId, List<ChatMessage> messages) {
		String key = HISTORY_PREFIX + memoryId;
		String json = ChatMessageSerializer.messagesToJson(messages);
		stringRedisTemplate.opsForValue().set(key, json);
	}

	/**
	 * 获取历史消息（前端显示用）
	 */
	@SuppressWarnings("null")
	public List<ChatMessage> getHistory(String memoryId) {
		String key = HISTORY_PREFIX + memoryId;
		String json = stringRedisTemplate.opsForValue().get(key);
		if (json == null || json.isEmpty()) {
			return List.of();
		}
		return ChatMessageDeserializer.messagesFromJson(json);
	}

	@Override
	public void deleteMessages(Object memoryId) {
		String key = CONTENT_PREFIX + memoryId.toString();
		stringRedisTemplate.delete(key);
		// 注意：不清除历史记录
	}
}
