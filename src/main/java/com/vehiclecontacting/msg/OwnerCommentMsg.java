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
@ApiModel(description = "帖子主人类")
public class OwnerCommentMsg {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty("帖子编号")
    private Long number;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty("帖子主人id")
    private Long fromId;

    @ApiModelProperty("帖子主人昵称")
    private String username;

    @ApiModelProperty("帖子主人头像")
    private String userPhoto;

    @ApiModelProperty("帖子主人性别")
    private String sex;

    @ApiModelProperty("主题")
    private String title;

    @ApiModelProperty("主人描述")
    private String description;

    @ApiModelProperty("描述图片1")
    private String photo1;

    @ApiModelProperty("描述图片2")
    private String photo2;

    @ApiModelProperty("描述图片3")
    private String photo3;

    @ApiModelProperty("帖子点赞次数")
    private Integer likeCounts;

    @ApiModelProperty("帖子评论数")
    private Integer commentCounts;

    @ApiModelProperty("帖子收藏次数")
    private Integer favorCounts;

    @ApiModelProperty("浏览量")
    private Integer scanCounts;

    @ApiModelProperty("帖子创建时间")
    private Date createTime;

    @ApiModelProperty("伪删除")
    private Integer deleted;

}
