package com.mostar.langchain4jtest.entity.dto;

import lombok.Data;

@Data
public class ChatSessionDTO {
    private String memoryId;
    private String title;
    private Long createTime;
}
