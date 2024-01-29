package com.inha.everytown.domain.chat.entity.relation;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "chat_room_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomTag {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag", nullable = false)
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Builder
    public ChatRoomTag(Long id, String tag, ChatRoom chatRoom) {
        this.id = id;
        this.tag = tag;
        this.chatRoom = chatRoom;
    }
}
