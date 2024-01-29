package com.inha.everytown.domain.place.service.relation;

import com.inha.everytown.domain.place.dto.PlaceMenuDto;
import com.inha.everytown.domain.place.entity.relation.Place;
import com.inha.everytown.domain.place.entity.relation.PlaceMenu;
import com.inha.everytown.domain.place.repository.relation.PlaceMenuRepository;
import com.inha.everytown.domain.place.repository.relation.PlaceRepository;
import com.inha.everytown.global.exception.CustomException;
import com.inha.everytown.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceMenuService {

    private final PlaceMenuRepository placeMenuRepository;
    private final PlaceRepository placeRepository;

    public boolean isMenuExist(Long placeId) {
        return placeMenuRepository.existsByPlace_Id(placeId);
    }

    public boolean isNeedCrawling(Long placeId) {
        Place place = placeRepository.findById(placeId).orElseThrow(
                () -> new CustomException(ErrorCode.PLACE_NO_SUCH_PLACE)
        );
        // 크롤링을 했나 안했나가 저장되므로 not 을 씌워준다
        return !place.getCrawling();
    }

    public List<PlaceMenuDto> getMenuDtoList(Long placeId) {
        return placeMenuRepository.findByPlace_Id(placeId).stream().map(PlaceMenuDto::EntityToDto).collect(Collectors.toList());
    }

    public List<PlaceMenuDto> saveMenuData(Long placeId, List<PlaceMenuDto> menuDtoList) {
        Place place = placeRepository.findById(placeId).orElseThrow(
                () -> new CustomException(ErrorCode.PLACE_NO_SUCH_PLACE)
        );

        List<PlaceMenu> saveMenuList = new ArrayList<>();
        for (PlaceMenuDto menuDto : menuDtoList) {
            PlaceMenu placeMenu = PlaceMenu.builder()
                    .name(menuDto.getName())
                    .price(menuDto.getPrice())
                    .place(place)
                    .build();
            saveMenuList.add(placeMenu);
        }
        List<PlaceMenu> responseMenuList = placeMenuRepository.saveAll(saveMenuList);
        place.updateCrawling();
        return responseMenuList.stream().map(PlaceMenuDto::EntityToDto).collect(Collectors.toList());
    }
}
