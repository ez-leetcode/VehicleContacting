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
@ApiModel(description = "历史记录类")
public class HistoryDiscussMsg {

    @ApiModelProperty("帖子编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long number;

    @ApiModelProperty("帖子主人id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("帖子主人昵称")
    private String username;

    @ApiModelProperty("帖子主人头像url")
    private String userPhoto;

    @ApiModelProperty("帖子主题")
    private String title;

    @ApiModelProperty("帖子内容")
    private String description;

    @ApiModelProperty("帖子图片")
    private String discussPhoto;

    @ApiModelProperty("新增评论量")
    private Integer newComments;

    @ApiModelProperty("收藏量")
    private Integer favorComments;

    @ApiModelProperty("最近浏览时间")
    private Date updateTime;

}
