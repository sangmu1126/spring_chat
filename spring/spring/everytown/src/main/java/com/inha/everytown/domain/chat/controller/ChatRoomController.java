package com.inha.everytown.domain.chat.controller;

import com.inha.everytown.domain.chat.dto.ChatDto;
import com.inha.everytown.domain.chat.dto.ChatMemberDto;
import com.inha.everytown.domain.chat.dto.ChatRoomReqDto;
import com.inha.everytown.domain.chat.dto.ChatRoomResDto;
import com.inha.everytown.domain.chat.service.ChatService;
import com.inha.everytown.global.jwt.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatRoomController {

    private final ChatService chatService;

    // 채팅방 목록
    @GetMapping("/rooms")
    public ResponseEntity<Page<ChatRoomResDto>> getRoomList(@RequestParam int page,
                                                            @RequestParam(defaultValue = "default") String tag,
                                                            @RequestParam double lat,
                                                            @RequestParam double lon) {

        Page<ChatRoomResDto> roomList = chatService.getRoomList(tag, page, lat, lon);
        return ResponseEntity.ok(roomList);
    }

    @GetMapping("/rooms/search")
    public ResponseEntity<Page<ChatRoomResDto>> search(@RequestParam int page,
                                                       @RequestParam String query,
                                                       @RequestParam(defaultValue = "default") String tag,
                                                       @RequestParam double lat,
                                                       @RequestParam double lon) {

        Page<ChatRoomResDto> searchList = chatService.searchRoomList(query, tag, page, lat, lon);
        return ResponseEntity.ok(searchList);
    }

    // 채팅방 생성
    @PostMapping("/room")
    public ResponseEntity<ChatRoomResDto> createRoom(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                     @RequestParam double lat,
                                                     @RequestParam double lon,
                                                     @RequestBody ChatRoomReqDto reqDto) {

        Long memberId = principalDetails.getMember().getId();
        ChatRoomResDto createdRoom = chatService.createRoom(reqDto, memberId, lat, lon);
        return ResponseEntity.ok(createdRoom);
    }

    // 채팅방 정보 불러오기
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ChatRoomResDto> getRoomInfo(@PathVariable Long roomId) {

        ChatRoomResDto room = chatService.getRoom(roomId);
        return ResponseEntity.ok(room);
    }

    // 채팅방 삭제
    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                            @PathVariable Long roomId) {

        Long memberId = principalDetails.getMember().getId();
        chatService.deleteRoom(memberId, roomId);
        return ResponseEntity.ok().build();
    }

    // 채팅방 입장 -> 인원수 늘림
    @GetMapping("/room/{roomId}/enter")
    public ResponseEntity<ChatRoomResDto> enterRoom(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                          @PathVariable Long roomId) {

        Long memberId = principalDetails.getMember().getId();
        chatService.joinMember(memberId, roomId);
        ChatRoomResDto room = chatService.getRoom(roomId);
        return ResponseEntity.ok(room);
    }

    // 현재 참여중인 인원
    @GetMapping("/room/{roomId}/memberCnt")
    public ResponseEntity<ChatMemberDto> getMemberCnt(@PathVariable Long roomId) {
        ChatMemberDto chatMemberDto = chatService.getRoomMemberList(roomId);
        return ResponseEntity.ok(chatMemberDto);
    }

    // 이전 기록 불러오기
    @GetMapping("/room/{roomId}/log")
    public ResponseEntity<List<ChatDto>> getMessageLog(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                       @PathVariable Long roomId) {

        Long memberId = principalDetails.getMember().getId();
        List<ChatDto> messageLog = chatService.getMessageLog(memberId, roomId);
        return ResponseEntity.ok(messageLog);
    }
}
