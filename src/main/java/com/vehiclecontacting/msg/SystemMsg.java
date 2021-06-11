package com.vehiclecontacting.msg;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "系统消息类")
public class SystemMsg {

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title",title);
        jsonObject.put("content",content);
        return jsonObject.toString();
    }
}
