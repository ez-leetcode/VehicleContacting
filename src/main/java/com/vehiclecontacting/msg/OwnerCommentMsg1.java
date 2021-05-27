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
@ApiModel(description = "评论主人类")
public class OwnerCommentMsg1 {

    @ApiModelProperty("评论编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long number;

    @ApiModelProperty("评论id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("评论昵称")
    private String username;

    @ApiModelProperty("评论头像")
    private String photo;

    @ApiModelProperty("评论人vip")
    private Integer vip;

    @ApiModelProperty("评论内容")
    private String comments;

    @ApiModelProperty("评论数")
    private Integer commentCounts;

    @ApiModelProperty("点赞数")
    private Integer likeCounts;

    @ApiModelProperty("评论时间")
    private Date createTime;

}
