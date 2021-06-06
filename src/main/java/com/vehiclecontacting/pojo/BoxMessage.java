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

@ApiModel(description = "消息盒子消息类")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BoxMessage {

    @ApiModelProperty("消息编号")
    @TableId(type = IdType.ID_WORKER)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long number;

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("内容")
    private String message;

    @ApiModelProperty("是否已读")
    private Integer isRead;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}