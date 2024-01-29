package com.inha.everytown.domain.restaurant.entity.relation;

import com.inha.everytown.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "restaurant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category_large_code", nullable = false)
    private String categoryLargeCode;

    @Column(name = "category_large_name", nullable = false)
    private String categoryLargeName;

    @Column(name = "category_middle_code", nullable = false)
    private String categoryMiddleCode;

    @Column(name = "category_middle_name", nullable = false)
    private String categoryMiddleName;

    @Column(name = "category_small_code", nullable = false)
    private String categorySmallCode;

    @Column(name = "category_small_name", nullable = false)
    private String categorySmallName;

    @Column(name = "dong", nullable = false)
    private String dong;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "longitude", nullable = false, columnDefinition = "decimal(18, 10)")
    private BigDecimal longitude;

    @Column(name = "latitude", nullable = false, columnDefinition = "decimal(18, 10)")
    private BigDecimal latitude;

    @Column(name = "crawling", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean crawling;

    @Column(name = "image", columnDefinition = "TEXT")
    private String image;

    @Builder
    public Restaurant(String name,
                      String categoryLargeCode,
                      String categoryLargeName,
                      String categoryMiddleCode,
                      String categoryMiddleName,
                      String categorySmallCode,
                      String categorySmallName,
                      String dong,
                      String address,
                      BigDecimal longitude,
                      BigDecimal latitude,
                      Boolean crawling,
                      String image) {

        this.name = name;
        this.categoryLargeCode = categoryLargeCode;
        this.categoryLargeName = categoryLargeName;
        this.categoryMiddleCode = categoryMiddleCode;
        this.categoryMiddleName = categoryMiddleName;
        this.categorySmallCode = categorySmallCode;
        this.categorySmallName = categorySmallName;
        this.dong = dong;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.crawling = crawling;
        this.image = image;
    }

    public void updateModifiedAt() {
        this.modifiedAt = LocalDateTime.now();
    }

    public void updateCrawling() {
        this.crawling = true;
        this.modifiedAt = LocalDateTime.now();
    }

    public void updateImage(String image) {
        this.image = image;
        this.modifiedAt = LocalDateTime.now();
    }
}
