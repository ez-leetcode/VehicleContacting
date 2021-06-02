package com.vehiclecontacting.msg;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@ApiModel(description = "用户聊天类，用户json parse")
public class TalkMsg {

    @ApiModelProperty("来自用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long fromId;

    @ApiModelProperty("去向用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long toId;

    @ApiModelProperty("信息")
    private String info;

}
