package com.inha.everytown.domain.etc.recommend.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

@Getter
@Setter
public class ItemLog {
    private Queue<Long> clickItem = new LinkedList<>();
    private Queue<Long> reviewItem = new LinkedList<>();
}
