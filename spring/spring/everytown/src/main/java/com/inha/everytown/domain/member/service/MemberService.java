package com.inha.everytown.domain.member.service;

import com.inha.everytown.domain.restaurant.dto.RestaurantReviewResDto;
import com.inha.everytown.domain.restaurant.entity.relation.RestaurantReview;
import com.inha.everytown.domain.restaurant.entity.relation.RestaurantReviewTag;
import com.inha.everytown.domain.restaurant.repository.relation.RestaurantReviewRepository;
import com.inha.everytown.domain.restaurant.repository.relation.RestaurantReviewTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

    private final RestaurantReviewRepository restaurantReviewRepository;
    private final RestaurantReviewTagRepository restaurantReviewTagRepository;

    public List<RestaurantReviewResDto> getMemberReviewList(Long memberId) {
        List<RestaurantReview> reviewList = restaurantReviewRepository.findByMember_Id(memberId);

        // review tag를 찾아서 조립
        List<RestaurantReviewResDto> resDtoList = new ArrayList<>();
        for (RestaurantReview review : reviewList) {
            RestaurantReviewResDto resDto = RestaurantReviewResDto.EntityToDto(review);
            List<RestaurantReviewTag> reviewTagList = restaurantReviewTagRepository.findByRestaurantReview_Id(review.getId());
            // reviewTag Entity에서 필요한 속성만 꺼내서 넣어줌
            resDto.setTag(reviewTagList.stream().map(
                    restaurantReviewTag -> restaurantReviewTag.getTag())
                    .collect(Collectors.toList())
            );
            resDtoList.add(resDto);
        }

        return resDtoList;
    }
}
