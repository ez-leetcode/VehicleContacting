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
@ApiModel(description = "车辆信息搜索类")
public class VehicleMsg {

    @ApiModelProperty("车辆牌照")
    private String license;

    @ApiModelProperty("车辆类型")
    private Integer type;

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("车辆品牌")
    private String vehicleBrand;

    @ApiModelProperty("用户昵称")
    private String username;

    @ApiModelProperty("用户头像url")
    private String photo;

    @ApiModelProperty("用户性别")
    private String sex;

    @ApiModelProperty("用户vip")
    private Integer vip;

    @ApiModelProperty("审核通过时间")
    private Date passTime;

}
