package com.inha.everytown.domain.member.entity;

import com.inha.everytown.global.entity.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "platform", nullable = false)
    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Column(name = "thumbnail", columnDefinition = "TEXT")
    private String thumbnail;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public Member(String nickname, String email, Platform platform, String thumbnail, Role role) {
        this.nickname = nickname;
        this.email = email;
        this.platform = platform;
        this.thumbnail = thumbnail;
        this.role = role;
    }

    public void update(String nickname, String email, String thumbnail) {
        this.nickname = nickname;
        this.email = email;
        this.thumbnail = thumbnail;
    }
}
