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

@ApiModel(description = "支付宝订单类")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AlipayOrder {

    @ApiModelProperty("支付宝订单编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableId(type = IdType.ID_WORKER)
    private Long number;

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("价格")
    private Double price;

    @ApiModelProperty("是否成功")
    private Integer isSuccess;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}