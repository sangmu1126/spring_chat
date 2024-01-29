package com.inha.everytown.domain.restaurant.controller;

import com.inha.everytown.domain.etc.recommend.repository.MemberPreferLogRepository;
import com.inha.everytown.domain.restaurant.dto.*;
import com.inha.everytown.domain.restaurant.entity.document.RestaurantDoc;
import com.inha.everytown.domain.etc.recommend.service.RecommendService;
import com.inha.everytown.domain.restaurant.service.document.RestaurantDocService;
import com.inha.everytown.domain.restaurant.service.relation.RestaurantMenuService;
import com.inha.everytown.domain.restaurant.service.relation.RestaurantReviewService;
import com.inha.everytown.domain.restaurant.service.relation.RestaurantService;
import com.inha.everytown.domain.etc.crawling.service.CrawlingService;
import com.inha.everytown.global.entity.Criteria;
import com.inha.everytown.global.jwt.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantMenuService restaurantMenuService;
    private final RestaurantService restaurantService;
    private final RestaurantDocService restaurantDocService;
    private final RestaurantReviewService restaurantReviewService;
    private final CrawlingService crawlingService;
    private final MemberPreferLogRepository restaurantLog;
    private final RecommendService recommendService;

    // 주위 1km 내의 음식점 받아오기 -> 정렬 기준은 criteria로 받는다
    @GetMapping
    public ResponseEntity<Page<RestaurantBasicInfoDto>> getRestaurantList(@RequestParam int page,
                                                                          @RequestParam(required = false) String cate,
                                                                          @RequestParam(defaultValue = "default") String tag,
                                                                          @RequestParam double lat,
                                                                          @RequestParam double lon,
                                                                          @RequestParam(defaultValue = "dist") Criteria criteria) {

        Page<RestaurantBasicInfoDto> restaurantList = restaurantDocService.getRestaurantDtoList(page, cate, tag, lat, lon, criteria);
        return ResponseEntity.ok(restaurantList);
    }

    // 1km 반경 내에서 검색 진행
    @GetMapping("/search")
    public ResponseEntity<Page<RestaurantBasicInfoDto>> search(@RequestParam String query,
                                                               @RequestParam(required = false) String cate,
                                                               @RequestParam(defaultValue = "default") String tag,
                                                               @RequestParam int page,
                                                               @RequestParam double lat,
                                                               @RequestParam double lon,
                                                               @RequestParam(defaultValue = "default") Criteria criteria) {

        log.info("search");
        Page<RestaurantBasicInfoDto> searchResult = restaurantDocService.getRestaurantDtoListByQuery(page, cate, tag, query, lat, lon, criteria);
        return ResponseEntity.ok(searchResult);
    }

    // 특정 아이템의 기본 정보 불러오기
    @GetMapping("/{restaurantId}/basicInfo" )
    public ResponseEntity<RestaurantBasicInfoDto> getBasicInfo(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                               @PathVariable Long restaurantId,
                                                               @RequestParam double lat,
                                                               @RequestParam double lon) {

        if (principalDetails != null) {
            Long memberId = principalDetails.getMember().getId();
            restaurantLog.saveClickLog(memberId, restaurantId, null);
        }
        RestaurantBasicInfoDto restaurantBasicInfoDto = restaurantDocService.getRestaurantDtoById(restaurantId, lat, lon);
        return ResponseEntity.ok(restaurantBasicInfoDto);
    }

    // 특정 아이템의 메뉴 정보 불러오기
    @GetMapping("/{restaurantId}/menuInfo")
    public ResponseEntity<List<RestaurantMenuDto>> getMenuList(@PathVariable Long restaurantId) {

        List<RestaurantMenuDto> menuResponse = null;

        RestaurantDoc restaurantDoc = restaurantDocService.getRestaurantDoc(restaurantId);
        // 메뉴 정보는 한번 저장하면 바뀔 일이 거의 없으니 elasticsearch에서 먼저 검색한다
        // 먼저 elastic에서 검색하여 있으면 바로 리턴
        if(!restaurantDoc.getMenu().isEmpty()) {
            menuResponse = restaurantDoc.getMenu().stream().map(RestaurantMenuDto::DocToDto).collect(Collectors.toList());
            log.info("[ menu in elastic ]");
        }
        // elastic에서 없으면 DB에서 검색 -> elastic과 DB의 동기화는 시간이 걸려 데이터 불일치 가능성 존재 떄문
        else if (restaurantMenuService.isMenuExist(restaurantId)) {
            menuResponse = restaurantMenuService.getMenuDtoList(restaurantId);
            log.info("[ menu in DB ]");
        }
        // elastic과 DB 모두 데이터가 없어야 크롤링 시작 -> 이렇게 안하면 중복하여 데이터를 저장할 위험이 있음
        // 크롤링이 필요한지 확인 -> 크롤링을 해도 데이터 없는 경우 있기에 시간 줄이기 위함
        else if (restaurantMenuService.isNeedCrawling(restaurantId)) {
            // 크롤링 성공시 menuDto 리스트를, 실패시 빈 리스트 반환
            List<RestaurantMenuDto> menuData = crawlingService.getRestaurantMenuData(restaurantId);
            menuResponse = restaurantMenuService.saveMenuData(restaurantId, menuData);
            log.info("[ menu by crawling ]");
        }
        // 이렇게 해도 메뉴가 없으면 빈 배열 반환
        else menuResponse = new ArrayList<>();
        return ResponseEntity.ok(menuResponse);
    }

    // 특정 아이템에 달린 리뷰 리스트 가져오기
    @GetMapping("/{restaurantId}/review")
    public ResponseEntity<List<RestaurantReviewResDto>> getReviewList(@PathVariable Long restaurantId) {

        List<RestaurantReviewResDto> response;
        // 리뷰 데이터는 언제든지 변경될 수 있다
        // elastic과 DB 동기화에 시간이 걸려서 DB에서 가져옴
        if (restaurantReviewService.existReviews(restaurantId)) {
            response = restaurantReviewService.getReviewDtoList(restaurantId);
            log.info("IN DB");
        }
        // 없으면 빈 배열로 반환
        else {
            response = new ArrayList<>();
        }
        return ResponseEntity.ok(response);
    }

    // 특정 아이템에 리뷰 저장하기
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{restaurantId}/review")
    public ResponseEntity<RestaurantReviewResDto> saveReview(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                             @PathVariable Long restaurantId,
                                                             @RequestBody RestaurantReviewReqDto reviewReq) {

        Long memberId = principalDetails.getMember().getId();
        restaurantLog.saveReviewLog(memberId, restaurantId, null);
        RestaurantReviewResDto saveResultDto = restaurantReviewService.saveReview(memberId, restaurantId, reviewReq);
        return ResponseEntity.ok(saveResultDto);
    }

    // 리뷰 가져오기 -> 리뷰 수정 등 필요할 수 있어서 일단 만듬, restaurantId 필요없는데 그냥 꼴 맞추려고 씀
    @GetMapping("{restaurantId}/review/{reviewId}")
    public ResponseEntity<RestaurantReviewResDto> getReview(@PathVariable Long restaurantId,
                                                            @PathVariable Long reviewId) {

        RestaurantReviewResDto resDto = restaurantReviewService.getReview(reviewId);
        return ResponseEntity.ok(resDto);
    }

    // 리뷰 수정하기
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("{restaurantId}/review/{reviewId}")
    public ResponseEntity<RestaurantReviewResDto> updateReview(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                               @PathVariable Long restaurantId,
                                                               @PathVariable Long reviewId,
                                                               @RequestBody RestaurantReviewReqDto reviewReq) {

        Long memberId = principalDetails.getMember().getId();
        RestaurantReviewResDto resDto = restaurantReviewService.updateReview(memberId, reviewId, restaurantId, reviewReq);
        return ResponseEntity.ok(resDto);
    }

    // 리뷰 삭제
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("{restaurantId}/review/{reviewId}")
    public ResponseEntity<Void> deleteReview(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                             @PathVariable Long restaurantId,
                                             @PathVariable Long reviewId) {

        Long memberId = principalDetails.getMember().getId();
        restaurantLog.deleteReviewLog(memberId, restaurantId, null);
        restaurantReviewService.deleteReview(memberId, reviewId, restaurantId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/recommend")
    public ResponseEntity<List<RestaurantBasicInfoDto>> getRecommendList(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                                         @RequestParam double lat,
                                                                         @RequestParam double lon) {

        Long memberId = principalDetails.getMember().getId();
        List<RestaurantBasicInfoDto> recommendList = recommendService.getRestaurantRecommend(memberId, lat, lon);
        return ResponseEntity.ok(recommendList);
    }

    @GetMapping("/{restaurantId}/image")
    public ResponseEntity<RestaurantBasicInfoDto> getImage(@PathVariable Long restaurantId,
                                                           @RequestParam double lat,
                                                           @RequestParam double lon) {

        String image = "";
        // 이미지 없으면 크롤링
        if(!restaurantService.isExistImage(restaurantId)) {
            image = crawlingService.getRestaurantImage(restaurantId);
            restaurantService.updateImage(restaurantId, image);
        }
        // 이미지 있으면 그냥 가져다 씀
        else {
            image = restaurantService.getImage(restaurantId);
        }
        RestaurantBasicInfoDto dto = restaurantDocService.getRestaurantDtoById(restaurantId, lat, lon);
        dto.setImage(image);
        return ResponseEntity.ok(dto);
    }
}
