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

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        //String key = userId + memoryId;
        String json = stringRedisTemplate.opsForValue().get(memoryId.toString());
        /*if (json == null) {
            友好提示
            return List.of();
        }*/
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        String json = ChatMessageSerializer.messagesToJson(list);
        // 移除过期时间，会话永不过期
        stringRedisTemplate.opsForValue().set(memoryId.toString(), json);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        stringRedisTemplate.delete(memoryId.toString());
    }
}
