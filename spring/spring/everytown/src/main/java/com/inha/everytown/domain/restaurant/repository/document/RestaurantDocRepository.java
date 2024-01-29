package com.inha.everytown.domain.restaurant.repository.document;

import com.inha.everytown.domain.restaurant.entity.document.RestaurantDoc;
import com.inha.everytown.domain.restaurant.entity.document.RestaurantMenuDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantDocRepository extends ElasticsearchRepository<RestaurantDoc, Long> {

    @Query("{" +
            "    \"bool\": {" +
            "        \"must\": [" +
            "            {" +
            "                \"match\": {" +
            "                    \"tag\": \"?0\"" +
            "                }" +
            "            }" +
            "        ]," +
            "        \"filter\": {" +
            "            \"geo_distance\": {" +
            "                \"distance\": \"1km\"," +
            "                \"location\": {" +
            "                    \"lat\": ?1," +
            "                    \"lon\": ?2" +
            "                }" +
            "            }" +
            "        }" +
            "    }" +
            "}")
    Page<RestaurantDoc> findNearby(String tag, double lat, double lon, Pageable pageable);

    @Query("{" +
            "    \"bool\": {" +
            "        \"must\": [" +
            "            {" +
            "                \"match\": {" +
            "                    \"category_middle_code\": \"?0\"" +
            "                }" +
            "            }," +
            "            {" +
            "                \"match\": {" +
            "                    \"tag\": \"?1\"" +
            "                }" +
            "            }" +
            "        ]," +
            "        \"filter\": {" +
            "            \"geo_distance\": {" +
            "                \"distance\": \"1km\"," +
            "                \"location\": {" +
            "                    \"lat\": ?2," +
            "                    \"lon\": ?3" +
            "                }" +
            "            }" +
            "        }" +
            "    }" +
            "}")
    Page<RestaurantDoc> findNearbyWithCategory(String category, String tag, double lat, double lon, Pageable pageable);

    @Query("{" +
            "    \"bool\": {" +
            "        \"must\": [" +
            "            {" +
            "                \"multi_match\": {" +
            "                    \"query\": \"?1\"," +
            "                    \"fields\": [" +
            "                        \"category_middle_name\"," +
            "                        \"category_small_name\"," +
            "                        \"name\"," +
            "                        \"menu_names\"" +
            "                    ]," +
            "                    \"type\": \"most_fields\"," +
            "                    \"operator\": \"or\"" +
            "                }" +
            "            }," +
            "            {" +
            "                \"match\": {" +
            "                    \"tag\": \"?0\"" +
            "                }" +
            "            }" +
            "        ]," +
            "        \"filter\": {" +
            "            \"geo_distance\": {" +
            "                \"distance\": \"1km\"," +
            "                \"location\": {" +
            "                    \"lat\": ?2," +
            "                    \"lon\": ?3" +
            "                }" +
            "            }" +
            "        }" +
            "    }" +
            "}")
    Page<RestaurantDoc> findByQuery(String tag, String query, double lat, double lon, Pageable pageable);

    @Query("{" +
            "    \"bool\": {" +
            "        \"must\": [" +
            "            {" +
            "                \"multi_match\": {" +
            "                    \"query\": \"?2\"," +
            "                    \"fields\": [" +
            "                        \"category_middle_name\"," +
            "                        \"category_small_name\"," +
            "                        \"name\"," +
            "                        \"menu_names\"" +
            "                    ]," +
            "                    \"type\": \"most_fields\"," +
            "                    \"operator\": \"or\"" +
            "                }" +
            "            }," +
            "            {" +
            "                \"match\": {" +
            "                    \"category_middle_code\": \"?0\"" +
            "                }" +
            "            }," +
            "            {" +
            "                \"match\": {" +
            "                    \"tag\": \"?1\"" +
            "                }" +
            "            }" +
            "        ]," +
            "        \"filter\": {" +
            "            \"geo_distance\": {" +
            "                \"distance\": \"1km\"," +
            "                \"location\": {" +
            "                    \"lat\": ?3," +
            "                    \"lon\": ?4" +
            "                }" +
            "            }" +
            "        }" +
            "    }" +
            "}")
    Page<RestaurantDoc> findByQueryWithCategory(String category, String tag, String query, double lat, double lon, Pageable pageable);
}
