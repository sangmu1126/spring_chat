package com.inha.everytown.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ChatRoomReqDto {

    private String name;
    private List<String> tag;
    private String address;

    @Builder
    public ChatRoomReqDto(String name, List<String> tag, String address) {
        this.name = name;
        this.tag = tag;
        this.address = address;
    }
}
