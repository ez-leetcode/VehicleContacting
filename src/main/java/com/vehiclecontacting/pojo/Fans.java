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

@ApiModel(description = "粉丝类")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Fans {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty("关注者")
    private Long fromId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty("被关注者")
    private Long toId;

    @ApiModelProperty("关注时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
