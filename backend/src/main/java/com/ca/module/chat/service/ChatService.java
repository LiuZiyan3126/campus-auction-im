package com.ca.module.chat.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.module.chat.vo.ConversationVO;
import com.ca.module.chat.vo.MessageVO;

import java.util.List;

public interface ChatService {
    ConversationVO createConversation(Long userId, Long targetUserId, Long productId);
    List<ConversationVO> getConversations(Long userId);
    IPage<MessageVO> getMessages(Long userId, Long conversationId, int page, int size);
    MessageVO sendMessage(Long senderId, Long conversationId, String content, String contentType);
    void markAsRead(Long userId, Long conversationId);
}
