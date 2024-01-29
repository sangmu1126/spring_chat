package com.inha.everytown.domain.chat.dto;

import com.inha.everytown.domain.chat.entity.document.ChatRoomDoc;
import com.inha.everytown.domain.chat.entity.relation.ChatRoom;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomResDto {

    private Long id;
    private String name;
    private Long memberId;
    private String nickname;
    private List<String> tag;
    private Integer memberCnt;
    private LocalDateTime created_at;
    private String address;

    @Builder
    public ChatRoomResDto(Long id, String name, Long memberId, String nickname, List<String> tag, Integer memberCnt, LocalDateTime created_at, String address) {
        this.id = id;
        this.name = name;
        this.memberId = memberId;
        this.nickname = nickname;
        this.tag = tag;
        this.memberCnt = memberCnt;
        this.created_at = created_at;
        this.address = address;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public void setMemberCnt(Integer memberCnt) {
        this.memberCnt = memberCnt;
    }

    public static ChatRoomResDto DocToDto(ChatRoomDoc chatRoomDoc) {
        return ChatRoomResDto.builder()
                .id(chatRoomDoc.getId())
                .name(chatRoomDoc.getName())
                .memberId(chatRoomDoc.getMemberId())
                .nickname(chatRoomDoc.getNickname())
                .tag(Arrays.asList(chatRoomDoc.getTag().replace(" default", " ").split(" ")))
                .created_at(chatRoomDoc.getCreated_at())
                .address(chatRoomDoc.getAddress())
                .build();
    }

    public static ChatRoomResDto EntityToDto(ChatRoom chatRoom) {
        return ChatRoomResDto.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .memberId(chatRoom.getMember().getId())
                .nickname(chatRoom.getMember().getNickname())
                .created_at(chatRoom.getCreatedAt())
                .address(chatRoom.getName())
                .build();
    }
}
