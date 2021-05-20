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
@ApiModel(description = "评论消息类（二级，父级）")
public class CommentMsg1 {

    @ApiModelProperty("评论编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long number;

    @ApiModelProperty("评论者id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("评论内容")
    private String comments;

    @ApiModelProperty("评论者昵称")
    private String username;

    @ApiModelProperty("评论者头像url")
    private String userPhoto;

    @ApiModelProperty("评论点赞数")
    private Integer likeCounts;

    @ApiModelProperty("评论者性别")
    private String sex;

    @ApiModelProperty("评论数")
    private Integer commentCounts;

    @ApiModelProperty(value = "回复的评论编号",name = "0为没有被回复评论")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyNumber;

    @ApiModelProperty(value = "回复的id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyId;

    @ApiModelProperty(value = "回复的昵称")
    private String replyUsername;

    @ApiModelProperty(value = "回复评论内容")
    private String replyComments;

    @ApiModelProperty("评论时间")
    private Date createTime;

}
