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
	}

	/**
	 * 将当前 AI 记忆追加到历史记录（/clear 时调用）
	 */
	@SuppressWarnings("null")
	public void appendToHistory(Object memoryId) {
		String contentKey = CONTENT_PREFIX + memoryId.toString();
		String historyKey = HISTORY_PREFIX + memoryId.toString();

		// 获取当前 AI 记忆
		String contentJson = stringRedisTemplate.opsForValue().get(contentKey);
		if (contentJson == null || contentJson.isEmpty()) {
			return; // 没有内容，不需要追加
		}

		List<ChatMessage> currentMessages = ChatMessageDeserializer.messagesFromJson(contentJson);
		if (currentMessages.isEmpty()) {
			return;
		}

		// 获取已有历史记录
		String historyJson = stringRedisTemplate.opsForValue().get(historyKey);
		List<ChatMessage> historyMessages = List.of();
		if (historyJson != null && !historyJson.isEmpty()) {
			historyMessages = ChatMessageDeserializer.messagesFromJson(historyJson);
		}

		// 追加新消息（去重：跳过已存在的消息）
		List<ChatMessage> mergedMessages = new java.util.ArrayList<>(historyMessages);
		for (ChatMessage msg : currentMessages) {
			// 简单去重：如果消息内容已存在则跳过
			boolean exists = mergedMessages.stream()
					.anyMatch(m -> m.toString().equals(msg.toString()));
			if (!exists) {
				mergedMessages.add(msg);
			}
		}

		// 保存合并后的历史记录
		String mergedJson = ChatMessageSerializer.messagesToJson(mergedMessages);
		stringRedisTemplate.opsForValue().set(historyKey, mergedJson);
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
