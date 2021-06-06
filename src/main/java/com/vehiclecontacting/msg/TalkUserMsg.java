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
@ApiModel(description = "聊天列表类")
public class TalkUserMsg {

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("用户昵称")
    private String username;

    @ApiModelProperty("用户vip")
    private Integer vip;

    @ApiModelProperty("用户头像")
    private String photo;

    @ApiModelProperty("还有几条没读")
    private Integer noReadCounts;

    @ApiModelProperty("最近的聊天语句")
    private String lastMessage;

    @ApiModelProperty("最近的更新时间")
    private Date updateTime;

}
