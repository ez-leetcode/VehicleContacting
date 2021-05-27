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
@ApiModel(description = "帖子主页面下面两个热评类")
public class FirstCommentMsg {

    @ApiModelProperty("热评人id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyId;

    @ApiModelProperty("热评人昵称")
    private String replyUsername;

    @ApiModelProperty("热评人头像")
    private String replyPhoto;

    @ApiModelProperty("热评人vip")
    private Integer replyVip;

    @ApiModelProperty("热评人评论编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long replyNumber;

    @ApiModelProperty("热评人内容")
    private String replyDescription;

    @ApiModelProperty("热评被点赞数")
    private Integer replyLikeCounts;

    @ApiModelProperty("评论时间")
    private Date replyTime;

    @ApiModelProperty("伪删除")
    private Integer deleted;

}
