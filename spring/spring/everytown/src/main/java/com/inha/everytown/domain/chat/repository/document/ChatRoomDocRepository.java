package com.inha.everytown.domain.chat.repository.document;

import com.inha.everytown.domain.chat.entity.document.ChatRoomDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ChatRoomDocRepository extends ElasticsearchRepository<ChatRoomDoc, Long> {

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
    Page<ChatRoomDoc> findNearBy(String tag, double lat, double lon, Pageable pageable);

    @Query("{" +
            "    \"bool\": {" +
            "        \"must\": [" +
            "            {" +
            "                \"multi_match\": {" +
            "                    \"query\": \"?0\"," +
            "                    \"fields\": [" +
            "                        \"name\"," +
            "                        \"nickname\"," +
            "                        \"tag\"" +
            "                    ]," +
            "                    \"type\": \"most_fields\"," +
            "                    \"operator\": \"or\"" +
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
    Page<ChatRoomDoc> findByQuery(String query, String tag, double lat, double lon, Pageable pageable);
}
