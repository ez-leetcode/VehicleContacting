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
@ApiModel(description = "车辆信息审核类")
public class VehicleJudgeMsg {

    @ApiModelProperty("车辆牌照")
    private String license;

    @ApiModelProperty("车辆类型")
    private Integer type;

    @ApiModelProperty("车辆品牌")
    private String vehicleBrand;

    @ApiModelProperty("用户id")
    private Long id;

    @ApiModelProperty("用户昵称")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String username;

    @ApiModelProperty("用户头像url")
    private String userPhoto;

    @ApiModelProperty("车辆图片url")
    private String vehiclePhoto;

    @ApiModelProperty("用户性别")
    private String sex;

    @ApiModelProperty("用户vip")
    private Integer vip;

    @ApiModelProperty("上次驳回原因")
    private String backReason;

    @ApiModelProperty("更新时间")
    private Date updateTime;

}
