package com.ca.module.chat.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationVO {
    private Long id;
    private Long otherUserId;
    private String otherUserName;
    private String otherAvatar;
    private Long productId;
    private String productTitle;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
}
