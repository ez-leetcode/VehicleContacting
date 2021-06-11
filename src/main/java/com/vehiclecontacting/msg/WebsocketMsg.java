package com.vehiclecontacting.msg;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "websocket聊天封装类")
public class WebsocketMsg {

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("头像")
    private String photo;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("信息")
    private String info;

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",id);
        jsonObject.put("photo",photo);
        jsonObject.put("username",username);
        jsonObject.put("info",info);
        return jsonObject.toString();
    }
}
