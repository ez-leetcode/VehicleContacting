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
@ApiModel(description = "评论消息类")
public class CommentMsg {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty("评论编号")
    private Long number;

    @ApiModelProperty("评论者id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("评论者昵称")
    private String username;

    @ApiModelProperty("评论者头像")
    private String userPhoto;

    @ApiModelProperty("评论者性别")
    private String sex;

    @ApiModelProperty("评论内容")
    private String comments;

    @ApiModelProperty("点赞数")
    private Integer likeCounts;

    @ApiModelProperty("评论数")
    private Integer commentCounts;

    @ApiModelProperty(value = "被回复的评论编号1",name = "0为没有被回复评论")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyNumber1;

    @ApiModelProperty(value = "被回复评论内容1")
    private String replyDescription1;

    @ApiModelProperty(value = "被回复评论用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyId1;

    @ApiModelProperty(value = "被回复评论用户昵称")
    private String replyUsername1;

    @ApiModelProperty(value = "二级回复用户昵称")
    private String secondReplyUsername1;

    @ApiModelProperty(value = "被回复的评论编号2",name = "0为没有被回复评论")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyNumber2;

    @ApiModelProperty(value = "被回复评论内容1")
    private String replyDescription2;

    @ApiModelProperty(value = "被回复评论用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyId2;

    @ApiModelProperty(value = "被回复评论用户昵称")
    private String replyUsername2;

    @ApiModelProperty(value = "二级回复用户昵称")
    private String secondReplyUsername2;

    @ApiModelProperty(value = "被回复的评论编号3",name = "0为没有被回复评论")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyNumber3;

    @ApiModelProperty(value = "被回复评论内容1")
    private String replyDescription3;

    @ApiModelProperty(value = "被回复评论用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyId3;

    @ApiModelProperty(value = "被回复评论用户昵称")
    private String replyUsername3;

    @ApiModelProperty(value = "二级回复用户昵称")
    private String secondReplyUsername3;

    @ApiModelProperty("创建日期")
    private Date createTime;

}