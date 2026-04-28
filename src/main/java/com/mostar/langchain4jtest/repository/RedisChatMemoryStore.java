package com.mostar.langchain4jtest.repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedisChatMemoryStore implements ChatMemoryStore {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String CONTENT_PREFIX = "chat:session:content:";

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = CONTENT_PREFIX + memoryId.toString();
        String json = stringRedisTemplate.opsForValue().get(key);
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        String key = CONTENT_PREFIX + memoryId.toString();
        String json = ChatMessageSerializer.messagesToJson(list);
        stringRedisTemplate.opsForValue().set(key, json);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = CONTENT_PREFIX + memoryId.toString();
        stringRedisTemplate.delete(key);
    }
}
