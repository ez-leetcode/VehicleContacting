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
@ApiModel(description = "申请加好友类")
public class PostFriendMsg {

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("用户昵称")
    private String username;

    @ApiModelProperty("用户性别")
    private String sex;

    @ApiModelProperty("用户vip")
    private Integer vip;

    @ApiModelProperty("用户头像")
    private String photo;

    @ApiModelProperty("申请内容")
    private String reason;

    @ApiModelProperty("是否审核通过")
    private Integer isPass;

    @ApiModelProperty("申请时间")
    private Date updateTime;

}
