package com.inha.everytown.domain.member.controller;

import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.domain.member.service.MemberService;
import com.inha.everytown.domain.restaurant.dto.RestaurantReviewResDto;
import com.inha.everytown.global.jwt.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/info/review")
    public ResponseEntity<List<RestaurantReviewResDto>> getMyReview(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long memberId = principalDetails.getMember().getId();
        List<RestaurantReviewResDto> reviewResDtoList = memberService.getMemberReviewList(memberId);
        return ResponseEntity.ok(reviewResDtoList);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/me")
    public ResponseEntity<Member> getMyInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Member member = principalDetails.getMember();
        return ResponseEntity.ok(member);
    }
}
