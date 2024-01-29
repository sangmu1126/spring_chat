package com.inha.everytown.domain.place.service.relation;

import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.domain.member.repository.MemberRepository;
import com.inha.everytown.domain.place.dto.PlaceReviewReqDto;
import com.inha.everytown.domain.place.dto.PlaceReviewResDto;
import com.inha.everytown.domain.place.entity.relation.Place;
import com.inha.everytown.domain.place.entity.relation.PlaceReview;
import com.inha.everytown.domain.place.entity.relation.PlaceReviewTag;
import com.inha.everytown.domain.place.entity.relation.PlaceTag;
import com.inha.everytown.domain.place.repository.relation.PlaceRepository;
import com.inha.everytown.domain.place.repository.relation.PlaceReviewRepository;
import com.inha.everytown.domain.place.repository.relation.PlaceReviewTagRepository;
import com.inha.everytown.domain.place.repository.relation.PlaceTagRepository;
import com.inha.everytown.global.exception.CustomException;
import com.inha.everytown.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceReviewService {

    private final PlaceRepository placeRepository;
    private final PlaceReviewRepository placeReviewRepository;
    private final PlaceReviewTagRepository placeReviewTagRepository;
    private final PlaceTagRepository placeTagRepository;
    private final MemberRepository memberRepository;

    public PlaceReviewResDto saveReview(Long memberId, Long placeId, PlaceReviewReqDto reviewReqDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.AUTH_MEMBER_NOT_FOUND)
        );
        Place place = placeRepository.findById(placeId).orElseThrow(
                () -> new CustomException(ErrorCode.PLACE_NO_SUCH_PLACE)
        );

        PlaceReview savedReview = placeReviewRepository.save(
                PlaceReview.builder()
                        .rating(reviewReqDto.getRating())
                        .content(reviewReqDto.getContent())
                        .place(place)
                        .member(member)
                        .nickname(member.getNickname())
                        .build()
        );

        List<PlaceReviewTag> saveReviewTagList = new ArrayList<>();
        if (reviewReqDto.getTag() != null) {
            for (String tag : reviewReqDto.getTag()) {
                PlaceReviewTag reviewTag = PlaceReviewTag.builder()
                        .tag(tag)
                        .placeReview(savedReview)
                        .place(place)
                        .build();
                saveReviewTagList.add(reviewTag);
            }
        }
        placeReviewTagRepository.saveAll(saveReviewTagList);
        updatePlaceTag(place);
        place.updateModifiedAt();

        PlaceReviewResDto saveResultDto = PlaceReviewResDto.EntityToDto(savedReview);
        saveResultDto.setTag(saveReviewTagList.stream().map(reviewTag -> reviewTag.getTag()).collect(Collectors.toList()));
        return saveResultDto;
    }

    public List<PlaceReviewResDto> getReviewDtoList(Long placeId) {
        List<PlaceReview> reviewList = placeReviewRepository.findByPlace_Id(placeId);

        List<PlaceReviewResDto> reviewDtoList = new ArrayList<>();
        // 리뷰 태그 조립
        for (PlaceReview review : reviewList) {
            PlaceReviewResDto reviewDto = PlaceReviewResDto.EntityToDto(review);

            List<PlaceReviewTag> placeReviewTagList = placeReviewTagRepository.findByPlaceReview_Id(review.getId());
            reviewDto.setTag(placeReviewTagList.stream().map(
                            reviewTag -> reviewTag.getTag()).collect(Collectors.toList())
            );
            reviewDtoList.add(reviewDto);
        }
        return reviewDtoList;
    }

    public PlaceReviewResDto getReview(Long reviewId) {
        PlaceReview placeReview = placeReviewRepository.findById(reviewId).orElseThrow(
                () -> new CustomException(ErrorCode.REVIEW_NO_SUCH_REVIEW)
        );

        List<PlaceReviewTag> reviewTagList = placeReviewTagRepository.findByPlaceReview_Id(reviewId);
        PlaceReviewResDto resDto = PlaceReviewResDto.EntityToDto(placeReview);
        resDto.setTag(reviewTagList.stream().map(reviewTag -> reviewTag.getTag()).collect(Collectors.toList()));

        return resDto;
    }

    public PlaceReviewResDto updateReview(Long memberId, Long reviewId, Long placeId, PlaceReviewReqDto reviewReqDto) {

        PlaceReview review = placeReviewRepository.findById(reviewId).orElseThrow(
                () -> new CustomException(ErrorCode.REVIEW_NO_SUCH_REVIEW)
        );

        if (!memberId.equals(review.getMember().getId())) throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        review.updateReview(reviewReqDto);

        placeReviewTagRepository.deleteByPlaceReview_Id(reviewId);
        Place place = placeRepository.findById(placeId).orElseThrow(
                () -> new CustomException(ErrorCode.PLACE_NO_SUCH_PLACE)
        );
        List<PlaceReviewTag> savedReviewTagList = new ArrayList<>();
        if (reviewReqDto.getTag() != null) {
            for (String tag : reviewReqDto.getTag()) {
                PlaceReviewTag reviewTag = PlaceReviewTag.builder()
                        .tag(tag)
                        .placeReview(review)
                        .place(place)
                        .build();
                savedReviewTagList.add(reviewTag);
            }
        }
        placeReviewTagRepository.saveAll(savedReviewTagList);
        updatePlaceTag(place);
        place.updateModifiedAt();

        PlaceReviewResDto resDto = PlaceReviewResDto.EntityToDto(review);
        resDto.setTag(savedReviewTagList.stream().map(reviewTag -> reviewTag.getTag()).collect(Collectors.toList()));
        return resDto;
    }

    public void deleteReview(Long memberId, Long reviewId, Long placeId) {

        PlaceReview review = placeReviewRepository.findById(reviewId).orElseThrow(
                () -> new CustomException(ErrorCode.REVIEW_NO_SUCH_REVIEW)
        );
        if(!memberId.equals(review.getMember().getId())) throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);

        placeReviewTagRepository.deleteByPlaceReview_Id(reviewId);
        placeReviewRepository.delete(review);

        Place place = placeRepository.findById(placeId).orElseThrow(
                () -> new CustomException(ErrorCode.PLACE_NO_SUCH_PLACE)
        );
        updatePlaceTag(place);
        place.updateModifiedAt();
    }

    public boolean existReviews(Long placeId) {
        return placeReviewRepository.existsByPlace_Id(placeId);
    }

    private void updatePlaceTag(Place place) {

        List<PlaceReviewTag> reviewTagList = placeReviewTagRepository.findByPlace_Id(place.getId());
        Map<String, Integer> tagCntMap = new HashMap<>();

        int totalCnt = reviewTagList.size();
        if(totalCnt < 5) return;

        List<String> tagList = new ArrayList<>();
        for (PlaceReviewTag reviewTag : reviewTagList) {
            String tag = reviewTag.getTag();
            Integer cnt = tagCntMap.get(tag);
            if (cnt == null) {
                cnt = 0;
                tagList.add(tag);
            }
            tagCntMap.put(tag, cnt + 1);
        }

        placeTagRepository.deleteByPlace_Id(place.getId());
        List<PlaceTag> savePlaceTagList = new ArrayList<>();
        for (String tag : tagList) {
            Integer cnt = tagCntMap.get(tag);
            if (cnt * 100 / totalCnt >= 30) {
                savePlaceTagList.add(
                        PlaceTag.builder()
                                .tag(tag)
                                .place(place)
                                .build()
                );
            }
        }
        placeTagRepository.saveAll(savePlaceTagList);
    }
}
