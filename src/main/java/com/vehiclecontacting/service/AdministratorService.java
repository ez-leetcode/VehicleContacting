package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;

public interface AdministratorService {

    String judgeVehicle(String license,Integer isPass,String reason);

    JSONObject getVehicleList(Long cnt,Long page,String keyword);

    String frozeUser(Long id, Integer minutes);

    String reopenUser(Long id);

    String deleteDiscuss(Long number,String reason);

    String frozeSpeak(Long id,Integer hours);

    JSONObject getFrozenUser(Long cnt,Long page);

    JSONObject getComplainVehicleList(Long id,Long cnt,Long page,Integer isPass,String keyword);

    String judgeComplainVehicle(Long id,Long number,Integer isPass,String backReason);
}
