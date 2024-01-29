package com.inha.everytown.domain.chat.repository.relation;

import com.inha.everytown.domain.chat.entity.relation.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoom_Id(Long roomId);

    void deleteByChatRoom_Id(Long roomId);
}
