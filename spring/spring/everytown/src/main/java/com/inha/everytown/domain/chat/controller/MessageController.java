package com.inha.everytown.domain.chat.controller;

import com.inha.everytown.domain.chat.dto.ChatDto;
import com.inha.everytown.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final SimpMessageSendingOperations sendingOperations;
    private final ChatService chatService;

    @MessageMapping("/chat/enter")
    public void enterMember(@Payload ChatDto chatDto) {
        chatDto.setMessage(chatDto.getNickName()+"님이 입장하였습니다.");
        chatService.saveMessage(chatDto);
        sendingOperations.convertAndSend("/sub/chat/room/" + chatDto.getRoomId(), chatDto);
    }

    @MessageMapping("/chat/send")
    public void sendMessage(@Payload ChatDto chatDto) {
        chatDto.setTime();
        chatService.saveMessage(chatDto);
        sendingOperations.convertAndSend("/sub/chat/room/" + chatDto.getRoomId(), chatDto);
    }

    @MessageMapping("/chat/leave")
    public void leaveMember(@Payload ChatDto chatDto) {
        chatDto.setMessage(chatDto.getNickName() + "님이 퇴장하였습니다.");
        chatService.deleteMember(chatDto.getMemberId(), chatDto.getRoomId());
        sendingOperations.convertAndSend("/sub/chat/room/" + chatDto.getRoomId(), chatDto);
    }
}
