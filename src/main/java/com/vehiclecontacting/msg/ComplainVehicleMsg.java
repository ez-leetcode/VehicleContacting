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
@ApiModel(description = "申诉车辆消息类")
public class ComplainVehicleMsg {

    @ApiModelProperty("申诉编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long number;

    @ApiModelProperty("申诉用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("申诉用户昵称")
    private String username;

    @ApiModelProperty("头像")
    private String userPhoto;

    @ApiModelProperty("vip")
    private Integer vip;

    @ApiModelProperty("车牌号")
    private String license;

    @ApiModelProperty("申诉原因")
    private String reason;

    @ApiModelProperty("是否通过")
    private Integer isPass;

    @ApiModelProperty("退回原因")
    private String backReason;

    @ApiModelProperty("创建时间")
    private Date createTime;

}
