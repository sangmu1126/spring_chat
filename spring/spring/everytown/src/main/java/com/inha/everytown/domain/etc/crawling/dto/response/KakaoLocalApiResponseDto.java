package com.inha.everytown.domain.etc.crawling.dto.response;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class KakaoLocalApiResponseDto {

    private Meta meta;
    private ArrayList<KakaoPlaceDocument> documents;

    @Getter
    public class Meta {
        private int total_count;
        private boolean is_end;
        private SameName same_name;

        @Getter
        public class SameName {
            private List<String> region;
            private String keyword;
            private String selected_region;
        }
    }
}
