package com.inha.everytown.domain.restaurant.service.document;

import com.inha.everytown.domain.restaurant.entity.relation.RestaurantReview;
import com.inha.everytown.domain.restaurant.repository.relation.RestaurantReviewRepository;
import com.inha.everytown.global.entity.Criteria;
import com.inha.everytown.domain.restaurant.dto.RestaurantBasicInfoDto;
import com.inha.everytown.domain.restaurant.entity.document.RestaurantDoc;
import com.inha.everytown.domain.restaurant.repository.document.RestaurantDocRepository;
import com.inha.everytown.global.exception.CustomException;
import com.inha.everytown.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.GeoDistanceOrder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantDocService {

    private final RestaurantDocRepository restaurantDocRepository;
    private final RestaurantReviewRepository restaurantReviewRepository;

    public Page<RestaurantBasicInfoDto> getRestaurantDtoList(int page, String category, String tag, double lat, double lon, Criteria criteria) {
        Pageable pageable = getPageable(page, criteria, lat, lon);

        Page<RestaurantDoc> restaurantDocList;
        if (category == null) restaurantDocList = restaurantDocRepository.findNearby(tag, lat, lon, pageable);
        else restaurantDocList = restaurantDocRepository.findNearbyWithCategory(category, tag, lat, lon, pageable);

        setDistance(restaurantDocList, lat, lon);

        Page<RestaurantBasicInfoDto> restaurantDtoList = restaurantDocList.map(RestaurantBasicInfoDto::DocToDto);
        log.info("FIND NEARBY : total element = {}, total page = {}", restaurantDtoList.getTotalElements(), restaurantDtoList.getTotalPages());

        return restaurantDtoList;
    }

    public Page<RestaurantBasicInfoDto> getRestaurantDtoListByQuery(int page, String category, String tag, String query, double lat, double lon, Criteria criteria) {
        Pageable pageable = getPageable(page, criteria, lat, lon);

        Page<RestaurantDoc> restaurantDocList;
        if (category == null) restaurantDocList = restaurantDocRepository.findByQuery(tag, query, lat, lon, pageable);
        else restaurantDocList = restaurantDocRepository.findByQueryWithCategory(category, tag, query, lat, lon, pageable);

        setDistance(restaurantDocList, lat, lon);

        Page<RestaurantBasicInfoDto> restaurantDtoList = restaurantDocList.map(RestaurantBasicInfoDto::DocToDto);
        log.info("FIND BY QUERY : total element = {}, total page = {}", restaurantDtoList.getTotalElements(), restaurantDtoList.getTotalPages());

        return restaurantDtoList;
    }

    public RestaurantDoc getRestaurantDoc(Long id) {
        RestaurantDoc restaurantDoc = restaurantDocRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.RESTAURANT_NO_SUCH_RESTAURANT)
        );

        return restaurantDoc;
    }

    // 리뷰 평점 계산해서 보냄
    public RestaurantBasicInfoDto getRestaurantDtoById(Long id, double lat, double lon) {
        RestaurantDoc restaurantDoc = getRestaurantDoc(id);
        restaurantDoc.setDistance(lat, lon);

        List<RestaurantReview> reviewList = restaurantReviewRepository.findByRestaurant_Id(id);
        double total = 0;
        if(!reviewList.isEmpty()){
            for (RestaurantReview review : reviewList) {
                total += review.getRating();
            }
            total /= reviewList.size();
            total = Math.round(total * 10.0) / 10.0;
        }
        RestaurantBasicInfoDto restaurantBasicInfoDto = RestaurantBasicInfoDto.DocToDto(restaurantDoc);
        restaurantBasicInfoDto.setRating(total);

        return restaurantBasicInfoDto;
    }

    private void setDistance(Page<RestaurantDoc> page, double lat, double lon) {
        for (RestaurantDoc restaurantDoc : page) {
            restaurantDoc.setDistance(lat, lon);
        }
    }

    private Pageable getPageable(int page, Criteria criteria, double lat, double lon) {
        Sort sort = null;
        if (criteria == Criteria.DEFAULT) {
            sort = Sort.by(Sort.Order.desc("_score" ));
        } else if (criteria == Criteria.DIST) {
            GeoPoint location = new GeoPoint(lat, lon);
            sort = Sort.by(new GeoDistanceOrder("location", location).withUnit("km"));
        } else if (criteria == Criteria.RATING){
            System.out.println("rating");
//            sort = Sort.by(Sort.Order.desc("rating" ));
            sort = Sort.by("rating").descending();
        }
        return PageRequest.of(page, 16, sort);
    }
}
