package com.inha.everytown.domain.chat.entity.document;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import javax.persistence.Id;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Document(indexName = "chatroom")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Mapping(mappingPath = "elastic/chatroom-mapping.json")
@Setting(settingPath = "elastic/analysis-setting.json")
public class ChatRoomDoc {

    @Id
    private Long id;

    @Field(name = "name")
    private String name;

    @Field(name = "location")
    private GeoPoint location;

    @Field(name = "member_id")
    private Long memberId;

    @Field(name = "nickname")
    private String nickname;

    @Field(name = "tag")
    private String tag;

    @Field(name = "created_at")
    private LocalDateTime created_at;

    @Field(name = "modified_at")
    private LocalDateTime modified_at;

    @Field(name = "address")
    private String address;

    public LocalDateTime getCreated_at() {
        ZonedDateTime zonedDateTimeUtc = ZonedDateTime.of(created_at, ZoneId.of("UTC"));
        ZonedDateTime zonedDateTimeKST = zonedDateTimeUtc.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        return zonedDateTimeKST.toLocalDateTime();
    }
}
