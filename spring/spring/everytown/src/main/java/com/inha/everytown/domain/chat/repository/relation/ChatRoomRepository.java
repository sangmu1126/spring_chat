package com.inha.everytown.domain.chat.repository.relation;

import com.inha.everytown.domain.chat.entity.relation.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}
