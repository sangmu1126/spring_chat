package com.inha.everytown.domain.member.repository;

import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.domain.member.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndPlatform(String email, Platform platform);
}
