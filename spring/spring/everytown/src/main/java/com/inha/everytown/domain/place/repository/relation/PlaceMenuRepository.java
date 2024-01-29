package com.inha.everytown.domain.place.repository.relation;

import com.inha.everytown.domain.place.entity.relation.PlaceMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceMenuRepository extends JpaRepository<PlaceMenu, Long> {

    boolean existsByPlace_Id(Long placeId);

    List<PlaceMenu> findByPlace_Id(Long placeId);
}
