package com.inha.everytown.domain.place.entity.relation;

import com.inha.everytown.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "place_review_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceReviewTag extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag", nullable = false)
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_review_id")
    private PlaceReview placeReview;

    @Builder
    public PlaceReviewTag(Long id, String tag, Place place, PlaceReview placeReview) {
        this.id = id;
        this.tag = tag;
        this.place = place;
        this.placeReview = placeReview;
    }
}
