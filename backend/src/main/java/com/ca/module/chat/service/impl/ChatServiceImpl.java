package com.ca.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ca.common.exception.BusinessException;
import com.ca.module.chat.dto.MessageDTO;
import com.ca.module.chat.entity.Conversation;
import com.ca.module.chat.entity.Message;
import com.ca.module.chat.mapper.ConversationMapper;
import com.ca.module.chat.mapper.MessageMapper;
import com.ca.module.chat.service.ChatService;
import com.ca.module.chat.vo.ConversationVO;
import com.ca.module.chat.vo.MessageVO;
import com.ca.module.product.entity.Product;
import com.ca.module.product.mapper.ProductMapper;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // 内存敏感词集合（生产应改为Redis或数据库加载）
    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "违禁品", "枪支", "弹药", "毒品", "赌博", "色情", "裸聊", "诈骗", "传销", "刷单"
    ));

    @Override
    @Transactional
    public ConversationVO createConversation(Long userId, Long targetUserId, Long productId) {
        if (userId.equals(targetUserId)) {
            throw BusinessException.badRequest("不能和自己聊天");
        }
        Long u1 = Math.min(userId, targetUserId);
        Long u2 = Math.max(userId, targetUserId);
        // 检查已有会话
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUser1Id, u1)
                .eq(Conversation::getUser2Id, u2);
        if (productId != null) wrapper.eq(Conversation::getProductId, productId);
        Conversation conv = conversationMapper.selectOne(wrapper);
        if (conv != null) {
            return buildConversationVO(conv, userId);
        }
        conv = new Conversation();
        conv.setUser1Id(u1);
        conv.setUser2Id(u2);
        conv.setProductId(productId);
        conv.setLastMessageAt(LocalDateTime.now());
        conversationMapper.insert(conv);
        return buildConversationVO(conv, userId);
    }

    @Override
    public List<ConversationVO> getConversations(Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<Conversation>()
                .and(w -> w.eq(Conversation::getUser1Id, userId).or().eq(Conversation::getUser2Id, userId))
                .orderByDesc(Conversation::getLastMessageAt);
        List<Conversation> list = conversationMapper.selectList(wrapper);
        return list.stream().map(c -> buildConversationVO(c, userId)).collect(Collectors.toList());
    }

    @Override
    public IPage<MessageVO> getMessages(Long userId, Long conversationId, int page, int size) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null || (!conv.getUser1Id().equals(userId) && !conv.getUser2Id().equals(userId))) {
            throw BusinessException.forbidden("无权查看此会话");
        }
        Page<Message> pager = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getCreatedAt);
        IPage<Message> result = messageMapper.selectPage(pager, wrapper);
        return result.convert(this::toMessageVO);
    }

    @Override
    @Transactional
    public MessageVO sendMessage(Long senderId, Long conversationId, String content, String contentType) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) throw BusinessException.notFound("会话不存在");
        if (!conv.getUser1Id().equals(senderId) && !conv.getUser2Id().equals(senderId)) {
            throw BusinessException.forbidden("不属于此会话");
        }
        // 敏感词过滤
        String filtered = filterSensitive(content);
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setContent(filtered);
        msg.setContentType(contentType != null ? contentType : "TEXT");
        msg.setIsRead(0);
        msg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(msg);

        // 更新最后消息时间
        conv.setLastMessageAt(msg.getCreatedAt());
        conversationMapper.updateById(conv);

        // WebSocket实时推送给双方
        Long receiverId = conv.getUser1Id().equals(senderId) ? conv.getUser2Id() : conv.getUser1Id();
        MessageVO vo = toMessageVO(msg);
        Map<String, Object> wsMsg = Map.of("type", "NEW_MESSAGE", "payload", vo, "timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/chat", wsMsg);
        messagingTemplate.convertAndSendToUser(String.valueOf(senderId), "/queue/chat", wsMsg);

        return vo;
    }

    @Override
    public void markAsRead(Long userId, Long conversationId) {
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<Message>()
                .eq(Message::getConversationId, conversationId)
                .ne(Message::getSenderId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1);
        messageMapper.update(null, updateWrapper);
    }

    private ConversationVO buildConversationVO(Conversation conv, Long currentUserId) {
        Long otherId = conv.getUser1Id().equals(currentUserId) ? conv.getUser2Id() : conv.getUser1Id();
        User other = userMapper.selectById(otherId);
        // 最后消息
        Message lastMsg = messageMapper.selectOne(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conv.getId())
                .orderByDesc(Message::getCreatedAt)
                .last("LIMIT 1"));
        // 未读数
        long unread = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conv.getId())
                .ne(Message::getSenderId, currentUserId)
                .eq(Message::getIsRead, 0));

        String productTitle = null;
        if (conv.getProductId() != null) {
            Product p = productMapper.selectById(conv.getProductId());
            if (p != null) productTitle = p.getTitle();
        }

        return ConversationVO.builder()
                .id(conv.getId())
                .otherUserId(otherId)
                .otherUserName(other != null ? other.getUsername() : "未知")
                .otherAvatar(other != null ? other.getAvatar() : null)
                .productId(conv.getProductId())
                .productTitle(productTitle)
                .lastMessage(lastMsg != null ? lastMsg.getContent() : null)
                .lastMessageAt(conv.getLastMessageAt())
                .unreadCount((int) unread)
                .build();
    }

    private MessageVO toMessageVO(Message msg) {
        User sender = userMapper.selectById(msg.getSenderId());
        return MessageVO.builder()
                .id(msg.getId())
                .conversationId(msg.getConversationId())
                .senderId(msg.getSenderId())
                .senderName(sender != null ? sender.getUsername() : "未知")
                .senderAvatar(sender != null ? sender.getAvatar() : null)
                .content(msg.getContent())
                .contentType(msg.getContentType())
                .isRead(msg.getIsRead())
                .createdAt(msg.getCreatedAt())
                .build();
    }

    private String filterSensitive(String text) {
        String result = text;
        for (String word : SENSITIVE_WORDS) {
            if (result.contains(word)) {
                result = result.replace(word, "***");
            }
        }
        return result;
    }
}
