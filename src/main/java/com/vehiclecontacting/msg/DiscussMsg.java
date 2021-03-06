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
@ApiModel(description = "帖子消息类")
public class DiscussMsg {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty("帖子编号")
    private Long number;

    @ApiModelProperty(value = "首页图片",notes = "没有就是null")
    private String photo;

    @ApiModelProperty("帖子主人昵称")
    private String username;

    @ApiModelProperty("帖子主人头像")
    private String userPhoto;

    @ApiModelProperty("帖子主题")
    private String title;

    @ApiModelProperty("帖子内容")
    private String description;

    @ApiModelProperty("帖子点赞次数")
    private Integer likeCounts;

    @ApiModelProperty("帖子评论数")
    private Integer commentCounts;

    @ApiModelProperty("帖子收藏次数")
    private Integer favorCounts;

    @ApiModelProperty("浏览量")
    private Integer scanCounts;

    @ApiModelProperty("最近更新时间")
    private Date updateTime;

}