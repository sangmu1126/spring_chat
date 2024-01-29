package com.inha.everytown.domain.place.repository.relation;

import com.inha.everytown.domain.place.entity.relation.PlaceTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, Long> {

    void deleteByPlace_Id(Long placeId);
}
