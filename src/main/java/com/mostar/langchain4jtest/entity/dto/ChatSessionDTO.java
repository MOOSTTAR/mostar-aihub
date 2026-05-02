package com.mostar.langchain4jtest.entity.dto;

import lombok.Data;

@Data
public class ChatSessionDTO {
	private String memoryId;
	private String title;
	private Long createTime;
	private Boolean isPinned;      // 新增：是否置顶
	private Long pinnedAt;         // 新增：置顶时间戳（用于排序）
}
