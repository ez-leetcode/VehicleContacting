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
@ApiModel(description = "用户联结类")
public class LinkUserMsg {

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("用户昵称")
    private String username;

    @ApiModelProperty("性别")
    private String sex;

    @ApiModelProperty("头像")
    private String photo;

    @ApiModelProperty("关系")
    private String relationship;

    @ApiModelProperty("自我介绍")
    private String introduction;

    @ApiModelProperty("车牌1")
    private String license1;

    @ApiModelProperty("车牌2")
    private String license2;

    @ApiModelProperty("车牌3")
    private String license3;

    @ApiModelProperty("车牌4")
    private String license4;

    @ApiModelProperty("创建时间")
    private Date createTime;

}