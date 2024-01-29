package com.inha.everytown.domain.chat.dto;

import com.inha.everytown.domain.chat.entity.relation.ChatMessage;
import com.inha.everytown.domain.chat.entity.relation.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatDto {

    private MessageType type;
    private Long roomId;
    private Long memberId;
    private String nickName;
    private String message;
    private LocalDateTime time;
    private String thumbnail;

    @Builder
    public ChatDto(MessageType type, Long roomId, Long memberId, String nickName, String message, LocalDateTime time, String thumbnail) {
        this.type = type;
        this.roomId = roomId;
        this.memberId = memberId;
        this.nickName = nickName;
        this.message = message;
        this.time = time;
        this.thumbnail = thumbnail;
    }

    public static ChatDto EntityToDto(ChatMessage chatMessage) {
        return ChatDto.builder()
                .type(MessageType.TALK)
//                .roomId(chatMessage.getChatRoom().getId())    // 없어도 되는 정보라
                .memberId(chatMessage.getMember().getId())
                .nickName(chatMessage.getMember().getNickname())
                .message(chatMessage.getMessage())
                .time(chatMessage.getCreatedAt())
                .thumbnail(chatMessage.getMember().getThumbnail())
                .build();
    }

    public void setTime() {
        this.time = LocalDateTime.now();
    }
}
