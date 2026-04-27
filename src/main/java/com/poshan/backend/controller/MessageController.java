package com.poshan.backend.controller;

import com.poshan.backend.dto.ChatMessageRequest;
import com.poshan.backend.dto.ChatMessageResponse;
import com.poshan.backend.dto.MessageConversationResponse;
import com.poshan.backend.dto.MessageThreadResponse;
import com.poshan.backend.security.AuthContext;
import com.poshan.backend.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final AuthContext authContext;

    public MessageController(MessageService messageService, AuthContext authContext) {
        this.messageService = messageService;
        this.authContext = authContext;
    }

    @GetMapping("/member")
    public List<MessageConversationResponse> getForMember() {
        return messageService.getConversationsForMember(authContext.requireMemberId());
    }

    @GetMapping("/member/{nutritionistId}")
    public MessageThreadResponse getMemberThread(@PathVariable Long nutritionistId) {
        return messageService.getThreadForMember(authContext.requireMemberId(), nutritionistId);
    }

    @PostMapping("/member/{nutritionistId}")
    public ChatMessageResponse sendAsMember(@PathVariable Long nutritionistId, @Valid @RequestBody ChatMessageRequest request) {
        return messageService.sendAsMember(authContext.requireMemberId(), nutritionistId, request);
    }

    @GetMapping("/nutritionist")
    public List<MessageConversationResponse> getForNutritionist() {
        return messageService.getConversationsForNutritionist(authContext.requireNutritionistId());
    }

    @GetMapping("/nutritionist/{memberId}")
    public MessageThreadResponse getNutritionistThread(@PathVariable Long memberId) {
        return messageService.getThreadForNutritionist(authContext.requireNutritionistId(), memberId);
    }

    @PostMapping("/nutritionist/{memberId}")
    public ChatMessageResponse sendAsNutritionist(@PathVariable Long memberId, @Valid @RequestBody ChatMessageRequest request) {
        return messageService.sendAsNutritionist(authContext.requireNutritionistId(), memberId, request);
    }
}
