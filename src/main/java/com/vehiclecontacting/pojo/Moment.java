package com.vehiclecontacting.pojo;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@ApiModel(description = "用户动态类")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Moment {

    @TableId(type = IdType.ID_WORKER)
    @ApiModelProperty("动态编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long number;

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("动态内容")
    private String description;

    @ApiModelProperty("点赞数")
    private Integer likeCounts;

    @ApiModelProperty("评论数")
    private Integer commentCounts;

    @ApiModelProperty("动态图片1")
    private String photo1;

    @ApiModelProperty("动态图片2")
    private String photo2;

    @ApiModelProperty("动态图片3")
    private String photo3;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
