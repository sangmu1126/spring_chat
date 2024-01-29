package com.inha.everytown.domain.etc.recommend.repository;

import com.inha.everytown.domain.etc.recommend.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class MemberPreferLogRepository {

    private static Map<Long, RestaurantLog> restaurantLogStore = new HashMap<>();
    private static Map<Long, PlaceLog> placeLogStore = new HashMap<>();

    public void saveClickLog(Long memberId, Long itemId, String cate) {
        ItemLog itemLog = getItemLog(memberId, cate);

        if(isThereSameItem(itemLog, itemId)) return;

        Queue<Long> reviewItem = itemLog.getReviewItem();
        Queue<Long> clickItem = itemLog.getClickItem();
        clickItem.offer(itemId);
        if(reviewItem.size() + clickItem.size() > 5) clickItem.poll();

        log.info("{} : {}", memberId, getItemList(memberId, cate));
    }

    public void saveReviewLog(Long memberId, Long itemId, String cate) {
        ItemLog itemLog = getItemLog(memberId, cate);

        if(isThereSameItem(itemLog, itemId)) return;

        Queue<Long> reviewItem = itemLog.getReviewItem();
        Queue<Long> clickItem = itemLog.getClickItem();
        reviewItem.offer(itemId);
        if(reviewItem.size() >3) reviewItem.poll();
        else if(reviewItem.size() + clickItem.size() > 5) clickItem.poll();

        log.info("{} : {}", memberId, getItemList(memberId, cate));
    }

    public void deleteReviewLog(Long memberId, Long itemId, String cate) {
        ItemLog itemLog = getItemLog(memberId, cate);

        Queue<Long> reviewItem = itemLog.getReviewItem();
        if(reviewItem.contains(itemId)) reviewItem.remove(itemId);

        log.info("{} : {}", memberId, getItemList(memberId, cate));
    }

    public List<Long> getItemList(Long memberId, String cate) {
        ItemLog itemLog = getItemLog(memberId, cate);
        List<Long> list = new ArrayList<>();
        list.addAll(itemLog.getClickItem());
        list.addAll(itemLog.getReviewItem());
        return list;
    }


    private List<Long> getItemList(ItemLog itemLog) {
        ItemLog item = itemLog;
        List<Long> list = new ArrayList<>();
        list.addAll(item.getClickItem());
        list.addAll(item.getReviewItem());
        return list;
    }

    private boolean isThereSameItem(ItemLog itemLog, Long itemId) {
        List<Long> itemList = getItemList(itemLog);
        return itemList.contains(itemId);
    }

    private ItemLog getItemLog(Long memberId, String cate) {
        ItemLog itemLog;

        // restaurant
        if (cate == null) {
            if(!restaurantLogStore.containsKey(memberId)) restaurantLogStore.put(memberId, new RestaurantLog());
            itemLog = restaurantLogStore.get(memberId).getItemLog();
        }
        // place -> category 까지 신경 써야함
        else {
            if(!placeLogStore.containsKey(memberId)) placeLogStore.put(memberId, new PlaceLog());
            PlaceLog placeLog = placeLogStore.get(memberId);

            if(!placeLog.getItemLog().containsKey(cate)) placeLog.getItemLog().put(cate, new ItemLog());
            itemLog = placeLog.getItemLog().get(cate);
        }
        return itemLog;
    }
}
