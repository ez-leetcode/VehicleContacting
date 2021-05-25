package com.vehiclecontacting.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@ApiModel(description = "举报用户类")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ComplainUser {

    @ApiModelProperty("举报用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long fromId;

    @ApiModelProperty("被举报用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long toId;

    @ApiModelProperty("举报标题")
    private String title;

    @ApiModelProperty("举报内容")
    private String description;

    @ApiModelProperty("举报图片1")
    private String complainPhoto1;

    @ApiModelProperty("举报图片2")
    private String complainPhoto2;

    @ApiModelProperty("举报图片3")
    private String complainPhoto3;

    @ApiModelProperty("举报时间")
    private Date createTime;

}
