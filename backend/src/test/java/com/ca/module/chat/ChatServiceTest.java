package com.ca.module.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ca.common.exception.BusinessException;
import com.ca.module.chat.entity.Conversation;
import com.ca.module.chat.entity.Message;
import com.ca.module.chat.mapper.ConversationMapper;
import com.ca.module.chat.mapper.MessageMapper;
import com.ca.module.chat.service.impl.ChatServiceImpl;
import com.ca.module.product.mapper.ProductMapper;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock ConversationMapper conversationMapper;
    @Mock MessageMapper messageMapper;
    @Mock UserMapper userMapper;
    @Mock ProductMapper productMapper;
    @Mock SimpMessagingTemplate messagingTemplate;
    @InjectMocks ChatServiceImpl chatService;

    // ===== 创建会话测试 =====

    @Test
    void testCreateConversation_SelfChat_ShouldThrowException() {
        assertThatThrownBy(() -> chatService.createConversation(1L, 1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能和自己聊天");
    }

    @Test
    void testCreateConversation_NewConversation_ShouldCreate() {
        when(conversationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(conversationMapper.insert(any(Conversation.class))).thenAnswer(inv -> {
            inv.getArgument(0, Conversation.class).setId(1L);
            return 1;
        });
        when(userMapper.selectById(2L)).thenReturn(createUser(2L, "卖家"));

        var result = chatService.createConversation(1L, 2L, null);

        assertThat(result.getOtherUserId()).isEqualTo(2L);
        verify(conversationMapper).insert(any(Conversation.class));
    }

    @Test
    void testCreateConversation_Existing_ShouldReturnExisting() {
        Conversation existing = new Conversation();
        existing.setId(1L);
        existing.setUser1Id(1L);
        existing.setUser2Id(2L);
        when(conversationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(userMapper.selectById(2L)).thenReturn(createUser(2L, "卖家"));

        var result = chatService.createConversation(1L, 2L, null);

        assertThat(result.getId()).isEqualTo(1L);
        verify(conversationMapper, never()).insert(any());
    }

    // ===== 敏感词过滤测试 =====

    @Test
    void testSendMessage_WithSensitiveWord_ShouldFilter() {
        Conversation conv = new Conversation();
        conv.setId(1L);
        conv.setUser1Id(1L);
        conv.setUser2Id(2L);
        when(conversationMapper.selectById(1L)).thenReturn(conv);
        when(messageMapper.insert(any(Message.class))).thenAnswer(inv -> {
            inv.getArgument(0, Message.class).setId(1L);
            return 1;
        });
        when(userMapper.selectById(1L)).thenReturn(createUser(1L, "买家"));

        var result = chatService.sendMessage(1L, 1L, "这是赌博内容", "TEXT");

        // 验证消息被过滤
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageMapper).insert(captor.capture());
        assertThat(captor.getValue().getContent()).doesNotContain("赌博");
        assertThat(captor.getValue().getContent()).contains("***");
    }

    // ===== 权限测试 =====

    @Test
    void testSendMessage_NotInConversation_ShouldThrowException() {
        Conversation conv = new Conversation();
        conv.setId(1L);
        conv.setUser1Id(2L);
        conv.setUser2Id(3L);
        when(conversationMapper.selectById(1L)).thenReturn(conv);

        assertThatThrownBy(() -> chatService.sendMessage(1L, 1L, "test", "TEXT"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不属于此会话");
    }

    // ===== 会话列表测试 =====

    @Test
    void testGetConversations_() {
        List<Conversation> list = new ArrayList<>();
        Conversation c = new Conversation();
        c.setId(1L);
        c.setUser1Id(1L);
        c.setUser2Id(2L);
        list.add(c);
        when(conversationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(list);
        when(userMapper.selectById(2L)).thenReturn(createUser(2L, "卖家"));

        var result = chatService.getConversations(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOtherUserId()).isEqualTo(2L);
    }

    private User createUser(Long id, String name) {
        User u = new User();
        u.setId(id);
        u.setUsername(name);
        return u;
    }
}
