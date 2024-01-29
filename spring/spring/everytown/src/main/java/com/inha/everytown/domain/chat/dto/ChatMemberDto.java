package com.inha.everytown.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ChatMemberDto {
    private List<String> memberNickNameList = new ArrayList<>();
    private Integer memberCnt;
}
