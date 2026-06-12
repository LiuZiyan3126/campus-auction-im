package com.ca.module.chat.dto;

import lombok.Data;

@Data
public class MessageDTO {
    private Long conversationId;
    private String content;
    private String contentType;  // TEXT / IMAGE
}
