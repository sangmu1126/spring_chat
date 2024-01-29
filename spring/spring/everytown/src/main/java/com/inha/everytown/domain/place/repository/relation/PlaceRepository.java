package com.inha.everytown.domain.place.repository.relation;

import com.inha.everytown.domain.place.entity.relation.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {


}
