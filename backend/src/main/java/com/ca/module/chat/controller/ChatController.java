package com.ca.module.chat.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.common.util.PageVO;
import com.ca.common.util.ResultVO;
import com.ca.module.chat.dto.ConvCreateDTO;
import com.ca.module.chat.dto.MessageDTO;
import com.ca.module.chat.service.ChatService;
import com.ca.module.chat.vo.ConversationVO;
import com.ca.module.chat.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // === REST API ===

    @GetMapping("/api/v1/conversations")
    @ResponseBody
    public ResultVO<List<ConversationVO>> getConversations(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(chatService.getConversations(userId));
    }

    @PostMapping("/api/v1/conversations")
    @ResponseBody
    public ResultVO<ConversationVO> createConversation(Authentication auth, @RequestBody ConvCreateDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(chatService.createConversation(userId, dto.getTargetUserId(), dto.getProductId()));
    }

    @GetMapping("/api/v1/conversations/{id}/messages")
    @ResponseBody
    public ResultVO<PageVO<MessageVO>> getMessages(Authentication auth,
                                                    @PathVariable Long id,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "50") int size) {
        Long userId = (Long) auth.getPrincipal();
        IPage<MessageVO> result = chatService.getMessages(userId, id, page, size);
        return ResultVO.ok(PageVO.of(result));
    }

    @PostMapping("/api/v1/conversations/{id}/messages")
    @ResponseBody
    public ResultVO<MessageVO> sendMessageHttp(Authentication auth,
                                                @PathVariable Long id,
                                                @RequestBody MessageDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(chatService.sendMessage(userId, id, dto.getContent(), dto.getContentType()));
    }

    @PutMapping("/api/v1/conversations/{id}/read")
    @ResponseBody
    public ResultVO<Void> markRead(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        chatService.markAsRead(userId, id);
        return ResultVO.ok(null);
    }

    // === WebSocket STOMP ===

    @MessageMapping("/chat.send")
    public void handleWSMessage(@Payload MessageDTO dto, Principal principal) {
        Long senderId = Long.parseLong(principal.getName());
        chatService.sendMessage(senderId, dto.getConversationId(), dto.getContent(), dto.getContentType());
    }
}
