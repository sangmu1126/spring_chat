package com.inha.everytown.domain.place.service.relation;

import com.inha.everytown.domain.place.entity.relation.Place;
import com.inha.everytown.domain.place.repository.relation.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceService {

    private final PlaceRepository placeRepository;

    public void updateImage(Long placeId, String image) {
        Place place = placeRepository.findById(placeId).get();
        place.updateImage(image);
    }

    public boolean isExistImage(Long placeId) {
        Place place = placeRepository.findById(placeId).get();
        if (place.getImage() == null || place.getImage().isEmpty()) return false;
        return true;
    }

    public String getImage(Long placeId) {
        Place place = placeRepository.findById(placeId).get();
        return place.getImage();
    }
}
