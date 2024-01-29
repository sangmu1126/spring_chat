package com.inha.everytown.domain.place.repository.document;

import com.inha.everytown.domain.place.entity.document.PlaceDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PlaceDocRepository extends ElasticsearchRepository<PlaceDoc, Long> {

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
    Page<PlaceDoc> findNearby(String tag, double lat, double lon, Pageable pageable);

@Query("{" +
        "    \"bool\": {" +
        "        \"must\": [" +
        "            {" +
        "                \"simple_query_string\": {" +
        "                    \"query\": \"?0\"," +
        "                    \"fields\": [\"category_middle_code\"]," +
        "                    \"default_operator\": \"OR\"" +
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
    Page<PlaceDoc> findNearbyWithCategory(String category, String tag, double lat, double lon, Pageable pageable);

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
    Page<PlaceDoc> findByQuery(String tag, String query, double lat, double lon, Pageable pageable);

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
            "                \"simple_query_string\": {" +
            "                    \"query\": \"?0\"," +
            "                    \"fields\": [\"category_middle_code\"]," +
            "                    \"default_operator\": \"OR\"" +
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
    Page<PlaceDoc> findByQueryWithCategory(String category, String tag, String query, double lat, double lon, Pageable pageable);
}
