package com.vehiclecontacting.pojo;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
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
@ApiModel(description = "用户类")
public class User {

    @ApiModelProperty(value = "用户编号",notes = "主键")
    private String id;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "性别")
    private String sex;

    @ApiModelProperty(value = "头像url",notes = "头像资源统一存放在阿里云oss /userPhoto中")
    private String photo;

    @ApiModelProperty(value = "邮箱",notes = "用户没设置，注册时默认用学号生成学校邮箱")
    private String email;

    @ApiModelProperty(value = "粉丝数",notes = "在查看个人信息中呈现(粉丝列表功能)")
    private Integer fansCounts;

    @ApiModelProperty(value = "关注数",notes = "在个人信息中呈现(关注列表功能)")
    private Integer followCounts;

    @ApiModelProperty(value = "是否被冻结")
    private Integer isFrozen;

    @ApiModelProperty("账号创建日期")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty("最近更新日期")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty("冻结时间")
    private Date frozenDate;

    @ApiModelProperty("解封时间")
    private Date reopenDate;

}
