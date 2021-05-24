package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;

public interface AdministratorService {

    String judgeVehicle(String license,Integer isPass,String reason);

    JSONObject getVehicleList(Long cnt,Long page,String keyword);

}
