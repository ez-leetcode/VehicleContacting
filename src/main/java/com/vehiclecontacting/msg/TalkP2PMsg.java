package com.vehiclecontacting.msg;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@ApiModel(description = "用户1对1聊天类")
public class TalkP2PMsg {

    @ApiModelProperty("聊天编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long number;

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long fromId;

    @ApiModelProperty("对方id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long toId;

    @ApiModelProperty("消息")
    private String info;

    @ApiModelProperty("创建时间")
    private Date createTime;

}
