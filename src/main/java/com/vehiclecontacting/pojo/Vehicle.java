package com.vehiclecontacting.pojo;


import com.baomidou.mybatisplus.annotation.*;
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
@ApiModel(description = "车辆类")
public class Vehicle {

    @TableId(type = IdType.INPUT)
    @ApiModelProperty("车辆牌照")
    private String license;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty("用户id")
    private Long id;

    @ApiModelProperty("车辆类型")
    private Integer type;

    @ApiModelProperty("车牌照片")
    private String licensePhoto;

    @ApiModelProperty("车辆照片1")
    private String vehiclePhoto1;

    @ApiModelProperty("车辆照片2")
    private String vehiclePhoto2;

    @ApiModelProperty("车辆照片3")
    private String vehiclePhoto3;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("是否审核通过")
    private Integer isPass;

    @ApiModelProperty("退回原因")
    private String backReason;

    @ApiModelProperty("伪删除")
    @TableLogic
    private Integer deleted;

    @ApiModelProperty("过审时间")
    private Date passTime;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty("最近更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

}
