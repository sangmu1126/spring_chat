package com.inha.everytown.domain.restaurant.entity.relation;

import com.inha.everytown.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "restaurant_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantTag extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag", nullable = false)
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Builder
    public RestaurantTag(Long id, String tag, Restaurant restaurant) {
        this.id = id;
        this.tag = tag;
        this.restaurant = restaurant;
    }
}
