package com.vehiclecontacting.utils;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.TalkResult;

import java.util.HashMap;

public class TalkResultUtils {

    private static final HashMap<String,Integer> talkResultMap = new HashMap<>();

    static {
        talkResultMap.put("sendInfoSuccess",200);
        talkResultMap.put("receiveInfoSuccess",200);
        talkResultMap.put("systemInfoSuccess",200);
    }

    //object是json数据，msg是状态
    public static TalkResult<JSONObject> getResult(JSONObject object, String msg){
        TalkResult<JSONObject> result = new TalkResult<>();
        result.setStatus(talkResultMap.get(msg));
        result.setMsg(msg);
        result.setData(object);
        return result;
    }

}