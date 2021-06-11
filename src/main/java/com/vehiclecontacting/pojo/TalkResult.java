package com.vehiclecontacting.pojo;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TalkResult<T> {

    //状态
    private Integer status;

    //消息
    private String msg;

    //数据
    private T data;

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status",status);
        jsonObject.put("msg",msg);
        jsonObject.put("data",data);
        return jsonObject.toString();
    }


}
