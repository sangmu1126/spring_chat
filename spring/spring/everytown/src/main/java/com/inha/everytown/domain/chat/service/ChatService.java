package com.inha.everytown.domain.chat.service;

import com.inha.everytown.domain.chat.dto.ChatDto;
import com.inha.everytown.domain.chat.dto.ChatMemberDto;
import com.inha.everytown.domain.chat.dto.ChatRoomReqDto;
import com.inha.everytown.domain.chat.dto.ChatRoomResDto;
import com.inha.everytown.domain.chat.entity.relation.*;
import com.inha.everytown.domain.chat.repository.document.ChatRoomDocRepository;
import com.inha.everytown.domain.chat.repository.relation.ChatMessageRepository;
import com.inha.everytown.domain.chat.repository.relation.ChatRoomMemberRepository;
import com.inha.everytown.domain.chat.repository.relation.ChatRoomRepository;
import com.inha.everytown.domain.chat.repository.relation.ChatRoomTagRepository;
import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    // 거리계산 용이하도록 elasticsearch에서 방 찾는다 -> 5초 마다 스케쥴링
    private final ChatRoomDocRepository chatRoomDocRepository;
    // 채팅방 생성은 DB를 통해서 함
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomTagRepository chatRoomTagRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;

    // 근처 채팅방 불러오기 -> 좌표 계산 때문에 elastic 사용
    public Page<ChatRoomResDto> getRoomList(String tag, int page, double lat, double lon) {
        Pageable pageable = getPageable(page);
        Page<ChatRoomResDto> roomList = chatRoomDocRepository.findNearBy(tag, lat, lon, pageable).map(ChatRoomResDto::DocToDto);
        // 참여 중인 맴버 수 채워주기
        for (ChatRoomResDto chatRoomResDto : roomList) {
            Integer memberCnt = getRoomMemberList(chatRoomResDto.getId()).getMemberCnt();
            chatRoomResDto.setMemberCnt(memberCnt);
        }
        return roomList;
    }

    // 검색
    public Page<ChatRoomResDto> searchRoomList(String query, String tag, int page, double lat, double lon) {
        Pageable pageable = getPageable(page);
        Page<ChatRoomResDto> roomList = chatRoomDocRepository.findByQuery(query, tag, lat, lon, pageable).map(ChatRoomResDto::DocToDto);
        // 참여 중인 맴버 수 채워주기
        for (ChatRoomResDto chatRoomResDto : roomList) {
            Integer memberCnt = getRoomMemberList(chatRoomResDto.getId()).getMemberCnt();
            chatRoomResDto.setMemberCnt(memberCnt);
        }
        return roomList;
    }

    // 채팅방 하나 불러오기 -> DB로 가져옴
    public ChatRoomResDto getRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow();
        ChatRoomResDto resDto = ChatRoomResDto.EntityToDto(chatRoom);
//        // 태그  -> 필요 없어서 제외
//        List<ChatRoomTag> tagList = chatRoomTagRepository.findByChatRoom_Id(roomId);
//        resDto.setTag(tagList.stream().map(tagObject -> tagObject.getTag()).collect(Collectors.toList()));
        return resDto;
    }

    // 채팅방 생성
    public ChatRoomResDto createRoom(ChatRoomReqDto reqDto, Long memberId, double lat, double lon) {
        Member member = memberRepository.findById(memberId).get();
        // 채팅방 저장
        ChatRoom savedRoom = chatRoomRepository.save(
                ChatRoom.builder()
                        .name(reqDto.getName())
                        .member(member)
                        .latitude(new BigDecimal(lat))
                        .longitude(new BigDecimal(lon))
                        .address(reqDto.getAddress())
                        .build()
        );

        // 채팅방에 달린 태그 저장
        List<ChatRoomTag> saveTagList = new ArrayList<>();
        if (reqDto.getTag() != null) {
            for (String tag : reqDto.getTag()) {
                ChatRoomTag chatRoomTag = ChatRoomTag.builder()
                        .tag(tag)
                        .chatRoom(savedRoom)
                        .build();
                saveTagList.add(chatRoomTag);
            }
        }
        chatRoomTagRepository.saveAll(saveTagList);
        // elastic은 modifiedAt에 반응하여 동기화함
        // BaseTimeEntity로 자동으로 업데이트 되지만 데이터 불일치 가능성 때문에 한번 더 시간 업데이트
        savedRoom.updateModifiedAt();

        ChatRoomResDto resDto = ChatRoomResDto.EntityToDto(savedRoom);
        resDto.setTag(saveTagList.stream().map(chatRoomTag -> chatRoomTag.getTag()).collect(Collectors.toList()));
        return resDto;
    }

    public void deleteRoom(Long memberId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).get();
        // 삭제 요청 멤버와 실제 생성 맴버와 일치 확인
        if(chatRoom.getMember().getId() != memberId) return;

        chatMessageRepository.deleteByChatRoom_Id(roomId);
        chatRoomTagRepository.deleteByChatRoom_Id(roomId);
        chatRoomMemberRepository.deleteByChatRoom_Id(roomId);
        chatRoomRepository.deleteById(roomId);

        chatRoomDocRepository.deleteById(roomId);
    }

    public void joinMember(Long memberId, Long roomId) {
        Member member = memberRepository.findById(memberId).get();
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).get();
        chatRoomMemberRepository.save(
                ChatRoomMember.builder()
                        .member(member)
                        .chatRoom(chatRoom)
                        .build()
        );
    }

    public ChatMemberDto getRoomMemberList(Long roomId) {
        List<ChatRoomMember> memberList = chatRoomMemberRepository.findByChatRoom_Id(roomId);
        ChatMemberDto chatMemberDto = new ChatMemberDto();
        for (ChatRoomMember member : memberList) {
            String nickname = member.getMember().getNickname();
            chatMemberDto.getMemberNickNameList().add(nickname);
        }
        chatMemberDto.setMemberCnt(memberList.size());
        return chatMemberDto;
    }

    public void deleteMember(Long memberId, Long roomId) {
        chatRoomMemberRepository.deleteByMember_IdAndChatRoom_Id(memberId, roomId);
    }

    public ChatMessage saveMessage(ChatDto chatDto) {
        Member member = memberRepository.findById(chatDto.getMemberId()).get();
        ChatRoom chatRoom = chatRoomRepository.findById(chatDto.getRoomId()).get();

        return chatMessageRepository.save(
                ChatMessage.builder()
                        .member(member)
                        .chatRoom(chatRoom)
                        .message(chatDto.getMessage())
                        .type(chatDto.getType())
                        .build()
        );
    }

    public List<ChatDto> getMessageLog(Long memberId, Long roomId) {
        List<ChatMessage> messageLog = chatMessageRepository.findByChatRoom_Id(roomId);

        // 현재 맴버가 처음으로 등장한 메시지 찾기
        Integer idx = null;
        for (int i = 0; i < messageLog.size(); i++) {
            ChatMessage log = messageLog.get(i);
            if (log.getMember().getId() == memberId) {
                idx = i;
                break;
            }
        }

        List<ChatMessage> resultLog = new ArrayList<>();
        if (idx != null) {
            for (int i = idx; i < messageLog.size(); i++) {
                ChatMessage log = messageLog.get(i);
                if (log.getType() == MessageType.TALK) resultLog.add(log);
            }
        }

        return resultLog.stream().map(ChatDto::EntityToDto).collect(Collectors.toList());
    }

    private Pageable getPageable(int page) {
        Sort sort = Sort.by(Sort.Order.desc("created_at"));
        return PageRequest.of(page, 16, sort);
    }
}
