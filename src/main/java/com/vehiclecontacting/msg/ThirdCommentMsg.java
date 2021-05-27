package com.vehiclecontacting.msg;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@ApiModel(description = "第三页评论类")
public class ThirdCommentMsg {

    @ApiModelProperty("评论编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long number;

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("用户昵称")
    private String username;

    @ApiModelProperty("用户头像")
    private String photo;

    @ApiModelProperty("用户vip")
    private Integer vip;

    @ApiModelProperty("评论内容")
    private String description;

    @ApiModelProperty("点赞数")
    private Integer likeCounts;

    @ApiModelProperty("回复编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyNumber;

    @ApiModelProperty("回复id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyId;

    @ApiModelProperty("回复昵称")
    private String replyUsername;

    @ApiModelProperty("评论时间")
    private Date createTime;

}
