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

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@ApiModel(description = "用户角色类，对应用户权限表")
public class UserRole {
    //注册的时候会添加角色

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty(value = "编号",notes = "雪花算法，唯一，数据库中作为主键")
    @TableId(type = IdType.ID_WORKER)
    private Long id;

    @ApiModelProperty(value = "用户id")
    private String user;

    @ApiModelProperty(value = "角色编号id")
    private Integer role;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty("最近一次更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
