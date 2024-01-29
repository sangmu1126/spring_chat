package com.inha.everytown.domain.place.entity.document;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Mapping(mappingPath = "elastic/place-mapping.json")
@Setting(settingPath = "elastic/analysis-setting.json")
public class PlaceDoc {

    @Id
    private Long id;

    @Field(name = "name")
    private String name;

    @Field(name = "category_large_code")
    private String categoryLargeCode;

    @Field(name = "category_large_name")
    private String categoryLargeName;

    @Field(name = "category_middle_code")
    private String categoryMiddleCode;

    @Field(name = "category_middle_name")
    private String categoryMiddleName;

    @Field(name = "category_small_code")
    private String categorySmallCode;

    @Field(name = "category_small_name")
    private String categorySmallName;

    @Field(name = "dong")
    private String dong;

    @Field(name = "address")
    private String address;

    @Field(name = "location")
    private GeoPoint location;

    @Field(name = "tag")
    private String tag;

    @Field(name = "menu", type = FieldType.Nested)
    private List<PlaceMenuDoc> menu;

    @Field(name = "review", type = FieldType.Nested)
    private List<PlaceReviewDoc> review;

    @Field(name = "created_at")
    private LocalDateTime created_at;

    @Field(name = "modified_at")
    private LocalDateTime modified_at;

    @Field(name = "rating")
    private Double rating;

    @Field(name = "distance")
    private Integer distance;

    private Integer reviewCnt;

    @Field(name = "image")
    private String image;

    public Double getRating() {
        this.rating = 0.0;
        if (!this.review.isEmpty()) {
            int cnt = 0;
            for (PlaceReviewDoc reviewDoc : this.review) {
                this.rating += reviewDoc.getRating();
                cnt++;
            }
            this.rating = this.rating / cnt;
            this.rating = Math.round(this.rating * 10.0) / 10.0;
        }
        return this.rating;
    }

    public Integer getReviewCnt() {
        this.reviewCnt = this.review.size();
        return reviewCnt;
    }

    public void setDistance(double lat, double lon) {
        double myLatRad = Math.toRadians(location.getLat());
        double myLonRad = Math.toRadians(location.getLon());
        double targetLatRad = Math.toRadians(lat);
        double targetLonRad = Math.toRadians(lon);

        double distLat = targetLatRad - myLatRad;
        double distLon = targetLonRad - myLonRad;
        double a = Math.sin(distLat / 2) * Math.sin(distLat / 2) + Math.cos(myLatRad) * Math.cos(targetLatRad) * Math.sin(distLon / 2) * Math.sin(distLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = 6371000 * c;

        this.distance = (int) distance;
    }
}
