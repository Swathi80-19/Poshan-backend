package com.poshan.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poshan.backend.entity.Appointment;
import com.poshan.backend.entity.AuthToken;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.enums.AppointmentMode;
import com.poshan.backend.enums.AppointmentStatus;
import com.poshan.backend.enums.Role;
import com.poshan.backend.repository.AppointmentRepository;
import com.poshan.backend.repository.AuthTokenRepository;
import com.poshan.backend.repository.ChatMessageRepository;
import com.poshan.backend.repository.MemberRepository;
import com.poshan.backend.repository.NutritionistRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.cors.allowed-origins=http://localhost:5173",
    "app.cors.allowed-origin-patterns=http://localhost:*"
})
class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NutritionistRepository nutritionistRepository;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        authTokenRepository.deleteAll();
        appointmentRepository.deleteAll();
        memberRepository.deleteAll();
        nutritionistRepository.deleteAll();
    }

    @Test
    void memberAndNutritionistSeeTheSameThread() throws Exception {
        Member member = new Member();
        member.setName("Riya Shah");
        member.setUsername("riya");
        member.setEmail("riya@example.com");
        member = memberRepository.save(member);

        Nutritionist nutritionist = new Nutritionist();
        nutritionist.setName("Asha Menon");
        nutritionist.setUsername("asha_menon");
        nutritionist.setEmail("asha@example.com");
        nutritionist.setSpecialization("Clinical Nutrition");
        nutritionist = nutritionistRepository.save(nutritionist);

        Appointment appointment = new Appointment();
        appointment.setMember(member);
        appointment.setNutritionist(nutritionist);
        appointment.setDateLabel("May 5");
        appointment.setTimeLabel("10:00 AM");
        appointment.setScheduledAt(LocalDateTime.now().plusDays(2));
        appointment.setMode(AppointmentMode.VIDEO);
        appointment.setStatus(AppointmentStatus.UPCOMING);
        appointmentRepository.save(appointment);

        AuthToken memberToken = new AuthToken();
        memberToken.setToken("member-token");
        memberToken.setRole(Role.MEMBER);
        memberToken.setMember(member);
        memberToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        memberToken.setRevoked(false);
        authTokenRepository.save(memberToken);

        AuthToken nutritionistToken = new AuthToken();
        nutritionistToken.setToken("nutritionist-token");
        nutritionistToken.setRole(Role.NUTRITIONIST);
        nutritionistToken.setNutritionist(nutritionist);
        nutritionistToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        nutritionistToken.setRevoked(false);
        authTokenRepository.save(nutritionistToken);

        mockMvc.perform(post("/api/messages/member/" + nutritionist.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer member-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"Hello doctor\"}"))
            .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/messages/nutritionist/" + member.getId() + "/chat-access")
                .header(HttpHeaders.AUTHORIZATION, "Bearer nutritionist-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"unlocked\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chatUnlocked").value(true));

        mockMvc.perform(post("/api/messages/member/" + nutritionist.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer member-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"Hello doctor\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.senderRole").value("MEMBER"))
            .andExpect(jsonPath("$.text").value("Hello doctor"));

        mockMvc.perform(post("/api/messages/nutritionist/" + member.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer nutritionist-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"Hi Riya, I can see your booking.\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.senderRole").value("NUTRITIONIST"));

        mockMvc.perform(get("/api/messages/member/" + nutritionist.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer member-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.counterpartName").value("Asha Menon"))
            .andExpect(jsonPath("$.chatUnlocked").value(true))
            .andExpect(jsonPath("$.messages.length()").value(2))
            .andExpect(jsonPath("$.messages[0].text").value("Hello doctor"))
            .andExpect(jsonPath("$.messages[1].text").value("Hi Riya, I can see your booking."));

        mockMvc.perform(get("/api/messages/nutritionist/" + member.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer nutritionist-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.counterpartName").value("Riya Shah"))
            .andExpect(jsonPath("$.messages.length()").value(2))
            .andExpect(jsonPath("$.messages[0].senderRole").value("MEMBER"))
            .andExpect(jsonPath("$.messages[1].senderRole").value("NUTRITIONIST"));

        mockMvc.perform(get("/api/messages/nutritionist")
                .header(HttpHeaders.AUTHORIZATION, "Bearer nutritionist-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].counterpartName").value("Riya Shah"))
            .andExpect(jsonPath("$[0].chatUnlocked").value(true))
            .andExpect(jsonPath("$[0].lastMessage").value("Hi Riya, I can see your booking."));
    }
}
