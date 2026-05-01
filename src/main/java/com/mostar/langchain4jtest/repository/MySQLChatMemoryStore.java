package com.mostar.langchain4jtest.repository;

import java.util.List;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

public class MySQLChatMemoryStore implements ChatMemoryStore {
	@Override
	public List<ChatMessage> getMessages(Object o) {
		return List.of();
	}

	@Override
	public void updateMessages(Object o, List<ChatMessage> list) {

	}

	@Override
	public void deleteMessages(Object o) {

	}
}
