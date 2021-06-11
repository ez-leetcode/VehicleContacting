package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface BoxService {

    JSONObject getAllBox(Long id,Long cnt,Long page,String keyword);

    String readAllBox(Long id);

    String deleteBoxMessage(Long id, List<Long> numbers);

    void addBoxMessage(Long id,String message,String title);
}
