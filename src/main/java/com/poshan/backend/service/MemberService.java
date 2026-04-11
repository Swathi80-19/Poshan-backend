package com.poshan.backend.service;

import com.poshan.backend.dto.ActivityLogRequest;
import com.poshan.backend.dto.ActivityLogResponse;
import com.poshan.backend.dto.DashboardResponse;
import com.poshan.backend.dto.FoodLogRequest;
import com.poshan.backend.dto.FoodLogResponse;
import com.poshan.backend.dto.MemberProfileRequest;
import com.poshan.backend.dto.MemberProfileResponse;
import com.poshan.backend.entity.ActivityLog;
import com.poshan.backend.entity.FoodLog;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.MemberProfile;
import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.repository.ActivityLogRepository;
import com.poshan.backend.repository.AppointmentRepository;
import com.poshan.backend.repository.AuthTokenRepository;
import com.poshan.backend.repository.FoodLogRepository;
import com.poshan.backend.repository.MemberProfileRepository;
import com.poshan.backend.repository.MemberRepository;
import com.poshan.backend.repository.PaymentRepository;
import com.poshan.backend.repository.ReportRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final FoodLogRepository foodLogRepository;
    private final ActivityLogRepository activityLogRepository;
    private final AuthTokenRepository authTokenRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final ReportRepository reportRepository;

    public MemberService(
        MemberRepository memberRepository,
        MemberProfileRepository memberProfileRepository,
        FoodLogRepository foodLogRepository,
        ActivityLogRepository activityLogRepository,
        AuthTokenRepository authTokenRepository,
        AppointmentRepository appointmentRepository,
        PaymentRepository paymentRepository,
        ReportRepository reportRepository
    ) {
        this.memberRepository = memberRepository;
        this.memberProfileRepository = memberProfileRepository;
        this.foodLogRepository = foodLogRepository;
        this.activityLogRepository = activityLogRepository;
        this.authTokenRepository = authTokenRepository;
        this.appointmentRepository = appointmentRepository;
        this.paymentRepository = paymentRepository;
        this.reportRepository = reportRepository;
    }

    public MemberProfileResponse getProfile(Long memberId) {
        Member member = getMember(memberId);
        MemberProfile profile = memberProfileRepository.findByMemberId(memberId).orElseGet(() -> {
            MemberProfile created = new MemberProfile();
            created.setMember(member);
            return memberProfileRepository.save(created);
        });
        return toProfileResponse(profile);
    }

    public MemberProfileResponse updateProfile(Long memberId, MemberProfileRequest request) {
        Member member = getMember(memberId);
        MemberProfile profile = memberProfileRepository.findByMemberId(memberId).orElseGet(MemberProfile::new);
        profile.setMember(member);
        profile.setAge(request.age());
        profile.setGender(request.gender());
        profile.setHeightCm(request.heightCm());
        profile.setCurrentWeightKg(request.currentWeightKg());
        profile.setTargetWeightKg(request.targetWeightKg());
        profile.setActivityLevel(request.activityLevel());
        profile.setGoalFocus(request.goalFocus());
        profile.setCalorieGoal(request.calorieGoal());
        profile.setProteinGoal(request.proteinGoal());
        profile.setCarbsGoal(request.carbsGoal());
        profile.setFatsGoal(request.fatsGoal());
        profile.setFiberGoal(request.fiberGoal());
        profile.setWaterGoal(request.waterGoal());
        profile.setStepGoal(request.stepGoal());
        profile.setActiveMinutesGoal(request.activeMinutesGoal());
        profile.setSleepGoal(request.sleepGoal());
        return toProfileResponse(memberProfileRepository.save(profile));
    }

    public List<FoodLogResponse> getFoodLogs(Long memberId) {
        getMember(memberId);
        return foodLogRepository.findAllByMemberIdOrderByLoggedAtDesc(memberId).stream()
            .map(this::toFoodLogResponse)
            .toList();
    }

    public FoodLogResponse addFoodLog(Long memberId, FoodLogRequest request) {
        Member member = getMember(memberId);
        FoodLog foodLog = new FoodLog();
        foodLog.setMember(member);
        foodLog.setDayLabel(request.dayLabel());
        foodLog.setMealType(request.mealType());
        foodLog.setFoodName(request.foodName());
        foodLog.setCalories(request.calories());
        foodLog.setProtein(request.protein());
        foodLog.setCarbs(request.carbs());
        foodLog.setFats(request.fats());
        foodLog.setFiber(request.fiber());
        foodLog.setLoggedAt(request.loggedAt() != null ? request.loggedAt() : LocalDateTime.now());
        return toFoodLogResponse(foodLogRepository.save(foodLog));
    }

    public List<ActivityLogResponse> getActivityLogs(Long memberId) {
        getMember(memberId);
        return activityLogRepository.findAllByMemberIdOrderByLoggedAtDesc(memberId).stream()
            .map(this::toActivityLogResponse)
            .toList();
    }

    public ActivityLogResponse addActivityLog(Long memberId, ActivityLogRequest request) {
        Member member = getMember(memberId);
        ActivityLog activityLog = new ActivityLog();
        activityLog.setMember(member);
        activityLog.setDayLabel(request.dayLabel());
        activityLog.setSteps(request.steps());
        activityLog.setActiveMinutes(request.activeMinutes());
        activityLog.setWater(request.water());
        activityLog.setSleepHours(request.sleepHours());
        activityLog.setSleepQuality(request.sleepQuality());
        activityLog.setWeight(request.weight());
        activityLog.setMood(request.mood());
        activityLog.setNotes(request.notes());
        activityLog.setLoggedAt(request.loggedAt() != null ? request.loggedAt() : LocalDateTime.now());
        return toActivityLogResponse(activityLogRepository.save(activityLog));
    }

    public DashboardResponse getMemberDashboard(Long memberId) {
        List<FoodLogResponse> foodLogs = getFoodLogs(memberId);
        List<ActivityLogResponse> activityLogs = getActivityLogs(memberId);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("foodLogs", foodLogs.size());
        summary.put("activityLogs", activityLogs.size());
        summary.put("latestFoodLog", foodLogs.isEmpty() ? null : foodLogs.getFirst());
        summary.put("latestActivityLog", activityLogs.isEmpty() ? null : activityLogs.getFirst());
        return new DashboardResponse(summary, foodLogs);
    }

    @Transactional
    public void deleteMemberAccount(Long memberId) {
        Member member = getMember(memberId);
        authTokenRepository.deleteAllByMemberId(memberId);
        appointmentRepository.deleteAllByMemberId(memberId);
        paymentRepository.deleteAllByMemberId(memberId);
        reportRepository.deleteAllByMemberId(memberId);
        memberRepository.delete(member);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
    }

    private MemberProfileResponse toProfileResponse(MemberProfile profile) {
        Nutritionist assignedNutritionist = profile.getAssignedNutritionist();
        return new MemberProfileResponse(
            profile.getMember().getId(),
            profile.getMember().getName(),
            assignedNutritionist != null ? assignedNutritionist.getId() : null,
            assignedNutritionist != null ? assignedNutritionist.getName() : null,
            profile.getAge(),
            profile.getGender(),
            profile.getHeightCm(),
            profile.getCurrentWeightKg(),
            profile.getTargetWeightKg(),
            profile.getActivityLevel(),
            profile.getGoalFocus(),
            profile.getCalorieGoal(),
            profile.getProteinGoal(),
            profile.getCarbsGoal(),
            profile.getFatsGoal(),
            profile.getFiberGoal(),
            profile.getWaterGoal(),
            profile.getStepGoal(),
            profile.getActiveMinutesGoal(),
            profile.getSleepGoal()
        );
    }

    private FoodLogResponse toFoodLogResponse(FoodLog foodLog) {
        return new FoodLogResponse(
            foodLog.getId(),
            foodLog.getDayLabel(),
            foodLog.getMealType(),
            foodLog.getFoodName(),
            foodLog.getCalories(),
            foodLog.getProtein(),
            foodLog.getCarbs(),
            foodLog.getFats(),
            foodLog.getFiber(),
            foodLog.getLoggedAt()
        );
    }

    private ActivityLogResponse toActivityLogResponse(ActivityLog activityLog) {
        return new ActivityLogResponse(
            activityLog.getId(),
            activityLog.getDayLabel(),
            activityLog.getSteps(),
            activityLog.getActiveMinutes(),
            activityLog.getWater(),
            activityLog.getSleepHours(),
            activityLog.getSleepQuality(),
            activityLog.getWeight(),
            activityLog.getMood(),
            activityLog.getNotes(),
            activityLog.getLoggedAt()
        );
    }
}
