package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface TalkService {

    JSONObject getTalkList(Long id, Long cnt, Long page);

    String allRead(Long id);

    String deletedTalk(Long fromId,Long toId);

    String isRead(Long fromId,Long toId);

    String deleteTalkMsg(Long fromId, Long toId, List<Long> numbers);

    JSONObject getP2PTalkList(Long fromId,Long toId,Long page,Long cnt);
}
