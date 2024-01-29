package com.inha.everytown.domain.chat.repository.relation;

import com.inha.everytown.domain.chat.entity.relation.ChatRoomTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomTagRepository extends JpaRepository<ChatRoomTag, Long> {

    List<ChatRoomTag> findByChatRoom_Id(Long chatRoomId);

    void deleteByChatRoom_Id(Long chatRoomId);
}
