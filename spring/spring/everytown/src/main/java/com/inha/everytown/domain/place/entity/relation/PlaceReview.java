package com.inha.everytown.domain.place.entity.relation;

import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.domain.place.dto.PlaceReviewReqDto;
import com.inha.everytown.domain.restaurant.entity.relation.Restaurant;
import com.inha.everytown.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "place_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceReview extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Builder
    public PlaceReview(Long id, Integer rating, String content, Place place, Member member, String nickname) {
        this.id = id;
        this.rating = rating;
        this.content = content;
        this.place = place;
        this.member = member;
        this.nickname = nickname;
    }

    public void updateReview(PlaceReviewReqDto reviewReqDto) {
        this.rating = reviewReqDto.getRating();
        this.content = reviewReqDto.getContent();
    }
}
