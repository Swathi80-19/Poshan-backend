package com.poshan.backend.controller;

import com.poshan.backend.dto.ActivityLogRequest;
import com.poshan.backend.dto.ActivityLogResponse;
import com.poshan.backend.dto.DashboardResponse;
import com.poshan.backend.dto.FoodLogRequest;
import com.poshan.backend.dto.FoodLogResponse;
import com.poshan.backend.dto.MemberProfileRequest;
import com.poshan.backend.dto.MemberProfileResponse;
import com.poshan.backend.security.AuthContext;
import com.poshan.backend.service.MemberService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final AuthContext authContext;

    public MemberController(MemberService memberService, AuthContext authContext) {
        this.memberService = memberService;
        this.authContext = authContext;
    }

    @GetMapping("/profile")
    public MemberProfileResponse getProfile() {
        return memberService.getProfile(authContext.requireMemberId());
    }

    @PutMapping("/profile")
    public MemberProfileResponse updateProfile(@RequestBody MemberProfileRequest request) {
        return memberService.updateProfile(authContext.requireMemberId(), request);
    }

    @GetMapping("/food-logs")
    public List<FoodLogResponse> getFoodLogs() {
        return memberService.getFoodLogs(authContext.requireMemberId());
    }

    @PostMapping("/food-logs")
    public FoodLogResponse addFoodLog(@RequestBody FoodLogRequest request) {
        return memberService.addFoodLog(authContext.requireMemberId(), request);
    }

    @GetMapping("/activity-logs")
    public List<ActivityLogResponse> getActivityLogs() {
        return memberService.getActivityLogs(authContext.requireMemberId());
    }

    @PostMapping("/activity-logs")
    public ActivityLogResponse addActivityLog(@RequestBody ActivityLogRequest request) {
        return memberService.addActivityLog(authContext.requireMemberId(), request);
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return memberService.getMemberDashboard(authContext.requireMemberId());
    }

    @DeleteMapping("/account")
    public void deleteAccount() {
        memberService.deleteMemberAccount(authContext.requireMemberId());
    }
}
