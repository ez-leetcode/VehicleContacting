package com.vehiclecontacting.pojo;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@ApiModel(description = "类")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FavorDiscuss {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty("收藏帖子编号")
    private Long number;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty("收藏者id")
    private Long id;

    @ApiModelProperty("收藏创建日期")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
