package com.inha.everytown.domain.place.dto;


import com.inha.everytown.domain.place.entity.document.PlaceMenuDoc;
import com.inha.everytown.domain.place.entity.relation.PlaceMenu;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceMenuDto {

    private Long id;
    private String name;
    private Integer price;

    @Builder
    public PlaceMenuDto(Long id, String name, Integer price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public static PlaceMenuDto DocToDto(PlaceMenuDoc placeMenuDoc) {
        return PlaceMenuDto.builder()
                .id(placeMenuDoc.getId())
                .name(placeMenuDoc.getName())
                .price(placeMenuDoc.getPrice())
                .build();
    }

    public static PlaceMenuDto EntityToDto(PlaceMenu placeMenu) {
        return PlaceMenuDto.builder()
                .id(placeMenu.getId())
                .name(placeMenu.getName())
                .price(placeMenu.getPrice())
                .build();
    }
}
