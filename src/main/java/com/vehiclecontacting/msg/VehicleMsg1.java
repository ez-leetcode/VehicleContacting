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
@ApiModel(description = "获取车辆列表类")
public class VehicleMsg1 {

    @ApiModelProperty("车牌")
    private String license;

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("车辆种类")
    private Integer type;

    @ApiModelProperty("车辆品牌")
    private String vehicleBrand;

    @ApiModelProperty("车牌图片")
    private String licensePhoto;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("是否通过")
    private Integer isPass;

}
