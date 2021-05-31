package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;

public interface TalkService {

    JSONObject getTalkList(Long id, Long cnt, Long page);

    String allRead(Long id);
}
