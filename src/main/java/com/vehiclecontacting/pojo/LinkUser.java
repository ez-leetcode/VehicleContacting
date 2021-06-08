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

@ApiModel(description = "关联用户类")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LinkUser {

    @ApiModelProperty("联结id1")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id1;

    @ApiModelProperty("联结id2")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id2;

    @ApiModelProperty("联结时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
