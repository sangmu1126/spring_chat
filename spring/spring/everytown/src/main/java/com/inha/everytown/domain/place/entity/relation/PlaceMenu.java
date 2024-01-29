package com.inha.everytown.domain.place.entity.relation;

import com.inha.everytown.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "place_menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceMenu extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price")
    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @Builder
    public PlaceMenu(Long id, String name, Integer price, Place place) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.place = place;
    }
}
