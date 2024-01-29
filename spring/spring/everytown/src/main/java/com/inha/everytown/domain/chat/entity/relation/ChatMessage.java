package com.inha.everytown.domain.chat.entity.relation;

import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.global.entity.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoom chatRoom;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Builder
    public ChatMessage(Long id, Member member, ChatRoom chatRoom, String message, MessageType type) {
        this.id = id;
        this.member = member;
        this.chatRoom = chatRoom;
        this.message = message;
        this.type = type;
    }
}
