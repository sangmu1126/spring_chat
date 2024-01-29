package com.inha.everytown.domain.place.repository.relation;

import com.inha.everytown.domain.place.entity.relation.PlaceReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceReviewRepository extends JpaRepository<PlaceReview, Long> {

    List<PlaceReview> findByPlace_Id(Long placeId);

    List<PlaceReview> findByMember_Id(Long memberId);

    boolean existsByPlace_Id(Long placeId);
}
