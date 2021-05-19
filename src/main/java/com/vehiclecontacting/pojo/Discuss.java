package com.vehiclecontacting.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@ApiModel(description = "论坛帖子类")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Discuss {

    @TableId(type = IdType.ID_WORKER)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty("帖子编号")
    private Long number;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty("帖子主人id")
    private Long fromId;

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

    @ApiModelProperty(value = "伪删除",notes = "只有管理员和楼主才能删帖")
    @TableLogic
    private Integer deleted;

    @ApiModelProperty("创建日期")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty("最近更新日期")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
