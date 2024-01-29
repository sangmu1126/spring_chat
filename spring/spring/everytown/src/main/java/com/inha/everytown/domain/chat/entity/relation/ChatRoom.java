package com.inha.everytown.domain.chat.entity.relation;

import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false ,columnDefinition = "TEXT")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "longitude", nullable = false, columnDefinition = "decimal(18, 10)")
    private BigDecimal longitude;

    @Column(name = "latitude", nullable = false, columnDefinition = "decimal(18, 10)")
    private BigDecimal latitude;

    @Column(name = "address", nullable = false)
    private String address;

    @Builder
    public ChatRoom(Long id, String name, Member member, BigDecimal longitude, BigDecimal latitude, String address) {
        this.id = id;
        this.name = name;
        this.member = member;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
    }

    public void updateModifiedAt() {
        this.modifiedAt = LocalDateTime.now();
        System.out.println(this.modifiedAt);
    }
}
