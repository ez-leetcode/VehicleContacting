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

@ApiModel(description = "好友类")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Friend {

    @ApiModelProperty("用户id1")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id1;

    @ApiModelProperty("用户id2")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id2;

    @ApiModelProperty("加好友时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
