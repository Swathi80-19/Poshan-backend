package com.poshan.backend.service;

import com.poshan.backend.dto.ChatMessageRequest;
import com.poshan.backend.dto.ChatMessageResponse;
import com.poshan.backend.dto.MessageConversationResponse;
import com.poshan.backend.dto.MessageThreadResponse;
import com.poshan.backend.entity.Appointment;
import com.poshan.backend.entity.ChatMessage;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.enums.AppointmentStatus;
import com.poshan.backend.enums.Role;
import com.poshan.backend.repository.AppointmentRepository;
import com.poshan.backend.repository.ChatMessageRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MessageService {

    private final AppointmentRepository appointmentRepository;
    private final ChatMessageRepository chatMessageRepository;

    public MessageService(AppointmentRepository appointmentRepository, ChatMessageRepository chatMessageRepository) {
        this.appointmentRepository = appointmentRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional(readOnly = true)
    public List<MessageConversationResponse> getConversationsForMember(Long memberId) {
        Map<Long, Appointment> latestByNutritionist = new LinkedHashMap<>();

        appointmentRepository.findAllByMemberIdOrderByScheduledAtDesc(memberId)
            .forEach(appointment -> latestByNutritionist.putIfAbsent(appointment.getNutritionist().getId(), appointment));

        return latestByNutritionist.values().stream()
            .map(this::toMemberConversation)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageConversationResponse> getConversationsForNutritionist(Long nutritionistId) {
        Map<Long, Appointment> latestByMember = new LinkedHashMap<>();

        appointmentRepository.findAllByNutritionistIdOrderByScheduledAtDesc(nutritionistId)
            .forEach(appointment -> latestByMember.putIfAbsent(appointment.getMember().getId(), appointment));

        return latestByMember.values().stream()
            .map(this::toNutritionistConversation)
            .toList();
    }

    @Transactional(readOnly = true)
    public MessageThreadResponse getThreadForMember(Long memberId, Long nutritionistId) {
        Appointment appointment = requireRelationship(memberId, nutritionistId);
        return new MessageThreadResponse(
            nutritionistId,
            appointment.getNutritionist().getName(),
            appointment.getNutritionist().getSpecialization(),
            appointment.getScheduledAt(),
            appointment.getDateLabel(),
            appointment.getTimeLabel(),
            appointment.getStatus() == AppointmentStatus.UPCOMING,
            Boolean.TRUE.equals(appointment.getChatUnlocked()),
            getMessages(memberId, nutritionistId)
        );
    }

    @Transactional(readOnly = true)
    public MessageThreadResponse getThreadForNutritionist(Long nutritionistId, Long memberId) {
        Appointment appointment = requireRelationship(memberId, nutritionistId);
        return new MessageThreadResponse(
            memberId,
            appointment.getMember().getName(),
            appointment.getMember().getUsername(),
            appointment.getScheduledAt(),
            appointment.getDateLabel(),
            appointment.getTimeLabel(),
            appointment.getStatus() == AppointmentStatus.UPCOMING,
            Boolean.TRUE.equals(appointment.getChatUnlocked()),
            getMessages(memberId, nutritionistId)
        );
    }

    @Transactional
    public ChatMessageResponse sendAsMember(Long memberId, Long nutritionistId, ChatMessageRequest request) {
        Appointment appointment = requireRelationship(memberId, nutritionistId);
        requireUnlocked(appointment);
        ChatMessage message = new ChatMessage();
        message.setMember(appointment.getMember());
        message.setNutritionist(appointment.getNutritionist());
        message.setSenderRole(Role.MEMBER);
        message.setText(request.text().trim());
        return toMessageResponse(chatMessageRepository.save(message));
    }

    @Transactional
    public ChatMessageResponse sendAsNutritionist(Long nutritionistId, Long memberId, ChatMessageRequest request) {
        Appointment appointment = requireRelationship(memberId, nutritionistId);
        requireUnlocked(appointment);
        ChatMessage message = new ChatMessage();
        message.setMember(appointment.getMember());
        message.setNutritionist(appointment.getNutritionist());
        message.setSenderRole(Role.NUTRITIONIST);
        message.setText(request.text().trim());
        return toMessageResponse(chatMessageRepository.save(message));
    }

    @Transactional
    public MessageThreadResponse updateChatAccess(Long nutritionistId, Long memberId, boolean unlocked) {
        Appointment appointment = requireRelationship(memberId, nutritionistId);
        appointment.setChatUnlocked(unlocked);
        appointmentRepository.save(appointment);
        return getThreadForNutritionist(nutritionistId, memberId);
    }

    private List<ChatMessageResponse> getMessages(Long memberId, Long nutritionistId) {
        return chatMessageRepository.findAllByMemberIdAndNutritionistIdOrderByCreatedAtAsc(memberId, nutritionistId).stream()
            .map(this::toMessageResponse)
            .toList();
    }

    private Appointment requireRelationship(Long memberId, Long nutritionistId) {
        return appointmentRepository.findFirstByMemberIdAndNutritionistIdOrderByScheduledAtDesc(memberId, nutritionistId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Messaging is available only after a member books this nutritionist."
            ));
    }

    private MessageConversationResponse toMemberConversation(Appointment appointment) {
        Long memberId = appointment.getMember().getId();
        Long nutritionistId = appointment.getNutritionist().getId();
        ChatMessage latestMessage = chatMessageRepository
            .findFirstByMemberIdAndNutritionistIdOrderByCreatedAtDesc(memberId, nutritionistId)
            .orElse(null);

        return new MessageConversationResponse(
            nutritionistId,
            appointment.getNutritionist().getName(),
            appointment.getNutritionist().getSpecialization(),
            latestMessage != null ? latestMessage.getText() : buildAppointmentFallback(appointment),
            latestMessage != null ? latestMessage.getCreatedAt() : appointment.getScheduledAt(),
            appointment.getScheduledAt(),
            appointment.getDateLabel(),
            appointment.getTimeLabel(),
            appointment.getStatus() == AppointmentStatus.UPCOMING,
            Boolean.TRUE.equals(appointment.getChatUnlocked())
        );
    }

    private MessageConversationResponse toNutritionistConversation(Appointment appointment) {
        Long memberId = appointment.getMember().getId();
        Long nutritionistId = appointment.getNutritionist().getId();
        ChatMessage latestMessage = chatMessageRepository
            .findFirstByMemberIdAndNutritionistIdOrderByCreatedAtDesc(memberId, nutritionistId)
            .orElse(null);

        return new MessageConversationResponse(
            memberId,
            appointment.getMember().getName(),
            appointment.getMember().getUsername(),
            latestMessage != null ? latestMessage.getText() : buildAppointmentFallback(appointment),
            latestMessage != null ? latestMessage.getCreatedAt() : appointment.getScheduledAt(),
            appointment.getScheduledAt(),
            appointment.getDateLabel(),
            appointment.getTimeLabel(),
            appointment.getStatus() == AppointmentStatus.UPCOMING,
            Boolean.TRUE.equals(appointment.getChatUnlocked())
        );
    }

    private String buildAppointmentFallback(Appointment appointment) {
        if (!Boolean.TRUE.equals(appointment.getChatUnlocked())) {
            return "Appointment booked. Waiting for the nutritionist to unlock chat.";
        }

        if (appointment.getDateLabel() != null && appointment.getTimeLabel() != null) {
            return "Appointment booked for " + appointment.getDateLabel() + " at " + appointment.getTimeLabel() + ".";
        }

        if (appointment.getDateLabel() != null) {
            return "Appointment booked for " + appointment.getDateLabel() + ".";
        }

        return "Appointment booked. Start the conversation here.";
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        boolean sentByMember = message.getSenderRole() == Role.MEMBER;
        Member member = message.getMember();
        Nutritionist nutritionist = message.getNutritionist();

        return new ChatMessageResponse(
            message.getId(),
            message.getSenderRole().name(),
            sentByMember ? member.getId() : nutritionist.getId(),
            sentByMember ? member.getName() : nutritionist.getName(),
            message.getText(),
            message.getCreatedAt()
        );
    }

    private void requireUnlocked(Appointment appointment) {
        if (!Boolean.TRUE.equals(appointment.getChatUnlocked())) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Chat is locked. The nutritionist must unlock this conversation before messages can be sent."
            );
        }
    }
}
