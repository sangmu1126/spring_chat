package com.inha.everytown.domain.chat.repository.relation;

import com.inha.everytown.domain.chat.entity.relation.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    void deleteByMember_IdAndChatRoom_Id(Long memberId, Long chatroomId);

    List<ChatRoomMember> findByChatRoom_Id(Long roomId);

    void deleteByChatRoom_Id(Long chatroomId);
}
