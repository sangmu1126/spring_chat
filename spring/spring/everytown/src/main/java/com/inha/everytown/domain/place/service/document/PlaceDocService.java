package com.inha.everytown.domain.place.service.document;

import com.inha.everytown.domain.place.dto.PlaceBasicInfoDto;
import com.inha.everytown.domain.place.entity.document.PlaceDoc;
import com.inha.everytown.domain.place.entity.relation.PlaceReview;
import com.inha.everytown.domain.place.repository.document.PlaceDocRepository;
import com.inha.everytown.domain.place.repository.relation.PlaceReviewRepository;
import com.inha.everytown.global.entity.Criteria;
import com.inha.everytown.global.exception.CustomException;
import com.inha.everytown.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.GeoDistanceOrder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceDocService {

    private final PlaceDocRepository placeDocRepository;
    private final PlaceReviewRepository placeReviewRepository;

    public Page<PlaceBasicInfoDto> getPlaceDtoList(int page, String category, String tag, double lat, double lon, Criteria criteria) {
        Pageable pageable = getPageable(page, criteria, lat, lon);

        Page<PlaceDoc> placeDocList;
        if(category == null) placeDocList = placeDocRepository.findNearby(tag, lat, lon, pageable);
        else placeDocList = placeDocRepository.findNearbyWithCategory(category, tag, lat, lon, pageable);

        setDistance(placeDocList, lat, lon);

        Page<PlaceBasicInfoDto> placeDtoList = placeDocList.map(PlaceBasicInfoDto::DocToDto);
        return placeDtoList;
    }

    public Page<PlaceBasicInfoDto> getPlaceDtoListByQuery(int page, String category, String tag, String query, double lat, double lon, Criteria criteria) {
        Pageable pageable = getPageable(page, criteria, lat, lon);

        Page<PlaceDoc> placeDocList;
        if(category == null) placeDocList = placeDocRepository.findByQuery(tag, query, lat, lon, pageable);
        else placeDocList = placeDocRepository.findByQueryWithCategory(category, tag, query, lat, lon, pageable);

        setDistance(placeDocList, lat, lon);

        Page<PlaceBasicInfoDto> placeDtoList = placeDocList.map(PlaceBasicInfoDto::DocToDto);
        return placeDtoList;
    }

    public PlaceDoc getPlaceDoc(Long id) {
        PlaceDoc placeDoc = placeDocRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.PLACE_NO_SUCH_PLACE)
        );
        return placeDoc;
    }

    public PlaceBasicInfoDto getPlaceDtoById(Long id, double lat, double lon) {
        PlaceDoc placeDoc = getPlaceDoc(id);
        placeDoc.setDistance(lat, lon);

        List<PlaceReview> reviewList = placeReviewRepository.findByPlace_Id(id);
        double total = 0;
        if(!reviewList.isEmpty()){
            for (PlaceReview review : reviewList) {
                total += review.getRating();
            }
            total /= reviewList.size();
            total = Math.round(total * 10.0) / 10.0;
        }
        PlaceBasicInfoDto placeBasicInfoDto = PlaceBasicInfoDto.DocToDto(placeDoc);
        placeBasicInfoDto.setRating(total);

        return placeBasicInfoDto;
    }

    private void setDistance(Page<PlaceDoc> page, double lat, double lon) {
        for (PlaceDoc placeDoc : page) {
            placeDoc.setDistance(lat, lon);
        }
    }

    private Pageable getPageable(int page, Criteria criteria, double lat, double lon) {
        Sort sort = null;
        if (criteria == Criteria.DEFAULT) {
            sort = Sort.by(Sort.Order.desc("_score"));
        } else if (criteria == Criteria.DIST) {
            GeoPoint location = new GeoPoint(lat, lon);
            sort = Sort.by(new GeoDistanceOrder("location", location).withUnit("km"));
        } else if (criteria == Criteria.RATING) {
            sort = Sort.by(Sort.Order.desc("rating"));
        }
        return PageRequest.of(page, 16, sort);
    }
}
