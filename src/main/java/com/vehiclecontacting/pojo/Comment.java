package com.vehiclecontacting.pojo;


import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@ApiModel(description = "评论类")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Comment {

    @TableId(type = IdType.ID_WORKER)
    @ApiModelProperty("评论编号")
    private Long number;

    @ApiModelProperty("评论者id")
    private Long id;

    @ApiModelProperty("帖子编号")
    private Long discussNumber;

    @ApiModelProperty("评论内容")
    private String comments;

    @ApiModelProperty("点赞数")
    private Integer likeCounts;

    @ApiModelProperty("评论数")
    private Integer commentCounts;

    @ApiModelProperty(value = "父级评论编号",name = "0为没有父级")
    private Long fatherNumber;

    @ApiModelProperty(value = "被回复的评论编号",name = "0为没有被回复评论")
    private Long replyNumber;

    @ApiModelProperty(value = "伪删除",notes = "只有楼主和管理员才能删除")
    @TableLogic
    private Integer deleted;

    @ApiModelProperty("创建日期")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
