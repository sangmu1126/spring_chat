package com.inha.everytown.domain.place.repository.relation;

import com.inha.everytown.domain.place.entity.relation.PlaceReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceReviewTagRepository extends JpaRepository<PlaceReviewTag, Long> {

    List<PlaceReviewTag> findByPlaceReview_Id(Long placeReviewId);

    List<PlaceReviewTag> findByPlace_Id(Long placeId);

    void deleteByPlaceReview_Id(Long placeReviewId);
}
