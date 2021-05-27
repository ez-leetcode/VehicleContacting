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
@ApiModel(description = "帖子评论类")
public class SecondCommentMsg {

    @ApiModelProperty("评论编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long number;

    @ApiModelProperty("评论人id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("评论人昵称")
    private String username;

    @ApiModelProperty("评论人头像")
    private String photo;

    @ApiModelProperty("评论人vip")
    private Integer vip;

    @ApiModelProperty("评论内容")
    private String description;

    @ApiModelProperty("点赞数")
    private Integer likeCounts;

    @ApiModelProperty("评论数")
    private Integer commentCounts;

    @ApiModelProperty("评论时间")
    private Date createTime;

    @ApiModelProperty("回复评论人编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyNumber1;

    @ApiModelProperty("回复评论人id1")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyId1;

    @ApiModelProperty("回复评论人昵称1")
    private String replyUsername1;

    @ApiModelProperty("回复评论人头像1")
    private String replyPhoto1;

    @ApiModelProperty("回复评论人vip1")
    private Integer replyVip1;

    @ApiModelProperty("回复评论人内容1")
    private String replyDescription1;

    @ApiModelProperty("回复评论人点赞次数1")
    private Integer replyLikeCounts1;

    @ApiModelProperty("回复评论人昵称1")
    private String secondReplyUsername1;

    @ApiModelProperty("回复评论人时间1")
    private Date replyCreateTime1;

    @ApiModelProperty("回复评论人编号2")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyNumber2;

    @ApiModelProperty("回复评论人id2")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyId2;

    @ApiModelProperty("回复评论人昵称2")
    private String replyUsername2;

    @ApiModelProperty("回复评论人头像2")
    private String replyPhoto2;

    @ApiModelProperty("回复评论人vip2")
    private Integer replyVip2;

    @ApiModelProperty("回复评论人内容2")
    private String replyDescription2;

    @ApiModelProperty("回复评论人点赞次数2")
    private Integer replyLikeCounts2;

    @ApiModelProperty("回复评论人昵称2")
    private String secondReplyUsername2;

    @ApiModelProperty("回复评论人时间2")
    private Date replyCreateTime2;

}
