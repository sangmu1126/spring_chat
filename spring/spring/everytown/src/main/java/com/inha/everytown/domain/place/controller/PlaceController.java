package com.inha.everytown.domain.place.controller;

import com.inha.everytown.domain.etc.crawling.service.CrawlingService;
import com.inha.everytown.domain.etc.recommend.repository.MemberPreferLogRepository;
import com.inha.everytown.domain.etc.recommend.service.RecommendService;
import com.inha.everytown.domain.place.dto.PlaceBasicInfoDto;
import com.inha.everytown.domain.place.dto.PlaceMenuDto;
import com.inha.everytown.domain.place.dto.PlaceReviewReqDto;
import com.inha.everytown.domain.place.dto.PlaceReviewResDto;
import com.inha.everytown.domain.place.entity.document.PlaceDoc;
import com.inha.everytown.domain.place.entity.relation.Place;
import com.inha.everytown.domain.place.service.document.PlaceDocService;
import com.inha.everytown.domain.place.service.relation.PlaceMenuService;
import com.inha.everytown.domain.place.service.relation.PlaceReviewService;
import com.inha.everytown.domain.place.service.relation.PlaceService;
import com.inha.everytown.global.entity.Criteria;
import com.inha.everytown.global.jwt.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/place")
public class PlaceController {

    private final PlaceDocService placeDocService;
    private final PlaceMenuService placeMenuService;
    private final PlaceReviewService placeReviewService;
    private final CrawlingService crawlingService;
    private final MemberPreferLogRepository placeLog;
    private final RecommendService recommendService;
    private final PlaceService placeService;

    @GetMapping
    public ResponseEntity<Page<PlaceBasicInfoDto>> getPlaceList(@RequestParam int page,
                                                                @RequestParam(required = false) String cate,
                                                                @RequestParam(defaultValue = "default") String tag,
                                                                @RequestParam double lat,
                                                                @RequestParam double lon,
                                                                @RequestParam(defaultValue = "dist") Criteria criteria) {

        Page<PlaceBasicInfoDto> placeDtoList = placeDocService.getPlaceDtoList(page, cate, tag, lat, lon, criteria);
        return ResponseEntity.ok(placeDtoList);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PlaceBasicInfoDto>> search(@RequestParam String query,
                                                          @RequestParam(required = false) String cate,
                                                          @RequestParam(defaultValue = "default") String tag,
                                                          @RequestParam int page,
                                                          @RequestParam double lat,
                                                          @RequestParam double lon,
                                                          @RequestParam(defaultValue = "default") Criteria criteria) {

        Page<PlaceBasicInfoDto> placeDtoListByQuery = placeDocService.getPlaceDtoListByQuery(page, cate, tag, query, lat, lon, criteria);
        return ResponseEntity.ok(placeDtoListByQuery);
    }

    @GetMapping("/{placeId}/basicInfo")
    public ResponseEntity<PlaceBasicInfoDto> getBasicInfo(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                          @PathVariable Long placeId,
                                                          @RequestParam double lat,
                                                          @RequestParam double lon) {


        PlaceBasicInfoDto placeBasicInfoDto = placeDocService.getPlaceDtoById(placeId, lat, lon);
        // 로그 저장
        if (principalDetails != null) {
            Long memberId = principalDetails.getMember().getId();
            placeLog.saveClickLog(memberId, placeId, placeBasicInfoDto.getCategoryMiddleCode());
        }
        return ResponseEntity.ok(placeBasicInfoDto);
    }

    // 코드는 조금 비효율적이더라도 일단 이해하기 쉽게 만듬 -> 나중에 리팩토링
    @GetMapping("/{placeId}/menuInfo")
    public ResponseEntity<List<PlaceMenuDto>> getMenuList(@PathVariable Long placeId) {

        List<PlaceMenuDto> menuResponse = null;

        PlaceDoc placeDoc = placeDocService.getPlaceDoc(placeId);
        // elastic이 가장 빠르니 여기서 부터 확인 -> 메뉴는 최초 한번만 하기에 동기화 문제 적음
        if (!placeDoc.getMenu().isEmpty()) {
            menuResponse = placeDoc.getMenu().stream().map(PlaceMenuDto::DocToDto).collect(Collectors.toList());
            log.info("[ menu in elastic ]");
        }
        // DB에서 확인
        else if (placeMenuService.isMenuExist(placeId)) {
            menuResponse = placeMenuService.getMenuDtoList(placeId);
            log.info("[ menu in DB ]");
        }
        // 크롤링이 필요한지 확인 -> 크롤링을 해도 데이터 없는 경우 있기에 시간 줄이기 위함
        else if (placeMenuService.isNeedCrawling(placeId)) {
            List<PlaceMenuDto> menuData = crawlingService.getPlaceMenuData(placeId);
            menuResponse = placeMenuService.saveMenuData(placeId, menuData);
            log.info("[ menu by crawling ]");
        }
        // 이렇게 해도 메뉴가 없으면 빈 배열 반환
        else menuResponse = new ArrayList<>();
        return ResponseEntity.ok(menuResponse);
    }

    @GetMapping("/{placeId}/review")
    public ResponseEntity<List<PlaceReviewResDto>> getReviewList(@PathVariable Long placeId) {

        List<PlaceReviewResDto> response;
        // 리뷰는 언제든지 변경될 수 있기 때문에 elastic 에서 검색 X
        if (placeReviewService.existReviews(placeId)) {
            response = placeReviewService.getReviewDtoList(placeId);
        }
        // 없으면 빈 배열로 반환
        else {
            response = new ArrayList<>();
        }
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{placeId}/review")
    public ResponseEntity<PlaceReviewResDto> saveReview(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                        @PathVariable Long placeId,
                                                        @RequestBody PlaceReviewReqDto reviewReqDto) {

        Long memberId = principalDetails.getMember().getId();
        PlaceReviewResDto saveResultDto = placeReviewService.saveReview(memberId, placeId, reviewReqDto);
        // 로그 저장
        PlaceBasicInfoDto place = placeDocService.getPlaceDtoById(placeId, 0, 0);// 좌표 정보는 필요 없어서 대충함
        placeLog.saveReviewLog(memberId, placeId, place.getCategoryMiddleCode());

        return ResponseEntity.ok(saveResultDto);
    }

    // placeId 필요 없는데 일단 꼴 맞추기 위함
    @GetMapping("/{placeId}/review/{reviewId}")
    public ResponseEntity<PlaceReviewResDto> getReview(@PathVariable Long placeId,
                                                       @PathVariable Long reviewId) {

        PlaceReviewResDto resDto = placeReviewService.getReview(reviewId);
        return ResponseEntity.ok(resDto);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{placeId}/review/{reviewId}")
    public ResponseEntity<PlaceReviewResDto> updateReview(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                          @PathVariable Long placeId,
                                                          @PathVariable Long reviewId,
                                                          @RequestBody PlaceReviewReqDto reviewReqDto) {

        Long memberId = principalDetails.getMember().getId();
        PlaceReviewResDto resDto = placeReviewService.updateReview(memberId, reviewId, placeId, reviewReqDto);
        return ResponseEntity.ok(resDto);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/{placeId}/review/{reviewId}")
    public ResponseEntity<Void> deleteReview(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                             @PathVariable Long placeId,
                                             @PathVariable Long reviewId) {

        Long memberId = principalDetails.getMember().getId();
        placeReviewService.deleteReview(memberId, reviewId, placeId);

        // 로그 삭제
        PlaceBasicInfoDto place = placeDocService.getPlaceDtoById(placeId, 0, 0);
        placeLog.deleteReviewLog(memberId, placeId, place.getCategoryMiddleCode());

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/recommend")
    public ResponseEntity<List<PlaceBasicInfoDto>> getRecommendList(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                                    @RequestParam double lat,
                                                                    @RequestParam double lon,
                                                                    @RequestParam String cate) {

        Long memberId= principalDetails.getMember().getId();
        List<PlaceBasicInfoDto> recommendList = recommendService.getPlaceRecommend(memberId, lat, lon, cate);
        return ResponseEntity.ok(recommendList);
    }

    @GetMapping("/{placeId}/image")
    public ResponseEntity<PlaceBasicInfoDto> getImage(@PathVariable Long placeId,
                                                      @RequestParam double lat,
                                                      @RequestParam double lon) {

        // 조금 비효율적이더라도 개발을 빠르게 하기 위해 그냥 이렇게함
        String image = "";
        // 이미지 없으면 크롤링
        if (!placeService.isExistImage(placeId)) {
            image = crawlingService.getPlaceImage(placeId);
            placeService.updateImage(placeId, image);
        }
        // 이미지 있으면 갖다 씀
        else {
            image = placeService.getImage(placeId);
        }
        PlaceBasicInfoDto dto = placeDocService.getPlaceDtoById(placeId, lat, lon);
        dto.setImage(image);
        return ResponseEntity.ok(dto);
    }
}
