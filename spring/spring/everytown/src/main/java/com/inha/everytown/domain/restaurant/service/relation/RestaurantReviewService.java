package com.inha.everytown.domain.restaurant.service.relation;

import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.domain.member.repository.MemberRepository;
import com.inha.everytown.domain.restaurant.dto.RestaurantReviewReqDto;
import com.inha.everytown.domain.restaurant.dto.RestaurantReviewResDto;
import com.inha.everytown.domain.restaurant.entity.relation.Restaurant;
import com.inha.everytown.domain.restaurant.entity.relation.RestaurantReview;
import com.inha.everytown.domain.restaurant.entity.relation.RestaurantReviewTag;
import com.inha.everytown.domain.restaurant.entity.relation.RestaurantTag;
import com.inha.everytown.domain.restaurant.repository.relation.RestaurantRepository;
import com.inha.everytown.domain.restaurant.repository.relation.RestaurantReviewRepository;
import com.inha.everytown.domain.restaurant.repository.relation.RestaurantReviewTagRepository;
import com.inha.everytown.domain.restaurant.repository.relation.RestaurantTagRepository;
import com.inha.everytown.global.exception.CustomException;
import com.inha.everytown.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class RestaurantReviewService {

    private final RestaurantReviewRepository restaurantReviewRepository;
    private final RestaurantReviewTagRepository restaurantReviewTagRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTagRepository restaurantTagRepository;
    private final MemberRepository memberRepository;

    public RestaurantReviewResDto saveReview(Long memberId, Long restaurantId, RestaurantReviewReqDto reviewReqDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.AUTH_MEMBER_NOT_FOUND)
        );
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new CustomException(ErrorCode.RESTAURANT_NO_SUCH_RESTAURANT)
        );

        // 리뷰 저장
        RestaurantReview savedReview = restaurantReviewRepository.save(
                RestaurantReview.builder()
                        .rating(reviewReqDto.getRating())
                        .content(reviewReqDto.getContent())
                        .restaurant(restaurant)
                        .member(member)
                        .nickname(member.getNickname())
                        .build()
        );

        // 태그 저장
        List<RestaurantReviewTag> saveReviewTagList = new ArrayList<>();
        if(reviewReqDto.getTag() != null) {
            for (String tag : reviewReqDto.getTag()) {
                RestaurantReviewTag reviewTag = RestaurantReviewTag.builder()
                        .tag(tag)
                        .restaurantReview(savedReview)
                        .restaurant(restaurant)
                        .build();
                saveReviewTagList.add(reviewTag);
            }
        }
        restaurantReviewTagRepository.saveAll(saveReviewTagList);
        updateRestaurantTag(restaurant);
        restaurant.updateModifiedAt();

        // 저장 결과를 dto로 반환 -> tag 정보가 없으면 그냥 빈 배열 반환
        RestaurantReviewResDto saveResultDto = RestaurantReviewResDto.EntityToDto(savedReview);
        saveResultDto.setTag(saveReviewTagList.stream().map(reviewTag -> reviewTag.getTag()).collect(Collectors.toList()));

        return saveResultDto;
    }

    public List<RestaurantReviewResDto> getReviewDtoList(Long restaurantId) {
        // 그냥 찾아서 반환 하는게 아니라 태그 속성도 채워 줘야한다.
        List<RestaurantReview> reviewList = restaurantReviewRepository.findByRestaurant_Id(restaurantId);

        // 일일이 찾아서 조립하여 dto list에 담는다
        List<RestaurantReviewResDto> reviewDtoList = new ArrayList<>();
        for (RestaurantReview review : reviewList) {
            RestaurantReviewResDto reviewDto = RestaurantReviewResDto.EntityToDto(review);
            // 리뷰 entity의 id로 reviewTag list를 찾는다
            List<RestaurantReviewTag> restaurantReviewTagList = restaurantReviewTagRepository.findByRestaurantReview_Id(review.getId());
            // reviewTag에서 하나하나 tag 필드를 꺼내서 review dto에 담는다
            // stream은 비어있는 리스트에 대해 빈 스트림을 만들어 결과적으로 nullPoint exception이 뜨지 X
            reviewDto.setTag(restaurantReviewTagList.stream().map(
                    restaurantReviewTag -> restaurantReviewTag.getTag())
                    .collect(Collectors.toList())
            );
            reviewDtoList.add(reviewDto);
        }
        return reviewDtoList;
    }

    public RestaurantReviewResDto getReview(Long reviewId) {
        RestaurantReview review = restaurantReviewRepository.findById(reviewId).orElseThrow(
                () -> new CustomException(ErrorCode.REVIEW_NO_SUCH_REVIEW)
        );

        List<RestaurantReviewTag> reviewTagList = restaurantReviewTagRepository.findByRestaurantReview_Id(reviewId);
        RestaurantReviewResDto resDto = RestaurantReviewResDto.EntityToDto(review);
        resDto.setTag(reviewTagList.stream().map(restaurantReviewTag -> restaurantReviewTag.getTag()).collect(Collectors.toList()));

        return resDto;
    }

    public RestaurantReviewResDto updateReview(Long memberId, Long reviewId, Long restaurantId, RestaurantReviewReqDto reviewReqDto) {

        RestaurantReview review = restaurantReviewRepository.findById(reviewId).orElseThrow(
                () -> new CustomException(ErrorCode.REVIEW_NO_SUCH_REVIEW)
        );
        // 사용자 맞는지 체크 후 업데이트
        if(!memberId.equals(review.getMember().getId())) throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        review.updateReview(reviewReqDto);

        // 귀찮아서 그냥 태그는 전체 삭제 후 다시 생성
        restaurantReviewTagRepository.deleteByRestaurantReview_Id(reviewId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new CustomException(ErrorCode.RESTAURANT_NO_SUCH_RESTAURANT)
        );
        List<RestaurantReviewTag> savedReviewTagList = new ArrayList<>();
        if(reviewReqDto.getTag() != null) {
            for (String tag : reviewReqDto.getTag()) {
                RestaurantReviewTag reviewTag = RestaurantReviewTag.builder()
                        .tag(tag)
                        .restaurantReview(review)
                        .restaurant(restaurant)
                        .build();
                savedReviewTagList.add(reviewTag);
            }
        }
        restaurantReviewTagRepository.saveAll(savedReviewTagList);
        updateRestaurantTag(restaurant);
        restaurant.updateModifiedAt();

        RestaurantReviewResDto resDto = RestaurantReviewResDto.EntityToDto(review);
        resDto.setTag(savedReviewTagList.stream().map(restaurantReviewTag -> restaurantReviewTag.getTag()).collect(Collectors.toList()));
        return resDto;
    }

    public void deleteReview(Long memberId, Long reviewId, Long restaurantId) {

        RestaurantReview review = restaurantReviewRepository.findById(reviewId).orElseThrow(
                () -> new CustomException(ErrorCode.REVIEW_NO_SUCH_REVIEW)
        );
        if(!memberId.equals(review.getMember().getId())) throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        // 태그 삭제
        restaurantReviewTagRepository.deleteByRestaurantReview_Id(reviewId);
        // 리뷰 삭제
        restaurantReviewRepository.delete(review);

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new CustomException(ErrorCode.RESTAURANT_NO_SUCH_RESTAURANT)
        );
        updateRestaurantTag(restaurant);
        restaurant.updateModifiedAt();
    }

    public boolean existReviews(Long restaurantId) {
        return restaurantReviewRepository.existsByRestaurant_Id(restaurantId);
    }


    // restaurant table에서 전체 리뷰 태그 대비 어떤 태그의 비율이 30%가 넘으면 restaurant의 속성에 해당 태그를 추가한다
    // 순전히 elasticsearch에서 혼밥, 감성 카페 등을 검색에 걸리도록 하기 위함
    // elasticsearch에서 동적으로 계산해서 처리하는 것이 너무 어려워서 트릭으로 이런 방법을 선택
    private void updateRestaurantTag(Restaurant restaurant) {
        // 음식점에 달린 전체 리뷰 태그 가져오기
        List<RestaurantReviewTag> reviewTagList = restaurantReviewTagRepository.findByRestaurant_Id(restaurant.getId());
        Map<String, Integer> tagCntMap = new HashMap<>();

        int totalCnt = reviewTagList.size();
        if (totalCnt < 5) return;

        // 각 태그 개수 확인
        List<String> tagList = new ArrayList<>();
        for (RestaurantReviewTag reviewTag : reviewTagList) {
            String tag = reviewTag.getTag();
            Integer cnt = tagCntMap.get(tag);
            if (cnt == null) {
                cnt = 0;
                tagList.add(tag);
            }
            tagCntMap.put(tag, cnt + 1);
        }

        // 각 태그에 대해 30 프로 이상이면 해당 태그를 음식점에 등록 -> 전체 삭제후 다시 등록
        restaurantTagRepository.deleteByRestaurant_Id(restaurant.getId());
        List<RestaurantTag> saveRestaurantTagList = new ArrayList<>();
        for (String tag : tagList) {
            Integer cnt = tagCntMap.get(tag);
            if (cnt * 100 / totalCnt >= 30) {
                saveRestaurantTagList.add(
                        RestaurantTag.builder()
                                .restaurant(restaurant)
                                .tag(tag)
                                .build()
                );
            }
        }
        restaurantTagRepository.saveAll(saveRestaurantTagList);
    }
}
