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
@ApiModel(description = "用户类")
public class User {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableId(type = IdType.ID_WORKER)
    @ApiModelProperty(value = "用户编号",notes = "主键")
    private Long id;

    @ApiModelProperty(value = "用户昵称")
    private String username;

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

    @ApiModelProperty(value = "自我介绍")
    private String introduction;

    @ApiModelProperty(value = "牌照数")
    private Integer licenseCounts;

    @ApiModelProperty(value = "粉丝数",notes = "在查看个人信息中呈现(粉丝列表功能)")
    private Integer fansCounts;

    @ApiModelProperty(value = "关注数",notes = "在个人信息中呈现(关注列表功能)")
    private Integer followCounts;

    @ApiModelProperty(value = "个人帖子数")
    private Integer discussCounts;

    @ApiModelProperty(value = "动态数")
    private Integer momentCounts;

    @ApiModelProperty(value = "被举报次数")
    private Integer complainCounts;

    @ApiModelProperty(value = "说脏话次数")
    private Integer dirtyCounts;

    @ApiModelProperty(value = "黑名单人数")
    private Integer blackCounts;

    @ApiModelProperty(value = "好友数")
    private Integer friendCounts;

    @ApiModelProperty(value = "消息盒子未读数")
    private Integer boxMessageCounts;

    @ApiModelProperty(value = "联结数")
    private Integer connectCounts;

    @ApiModelProperty(value = "是否免打扰")
    private Integer isNoDisturb;

    @ApiModelProperty(value = "vip")
    private Integer vip;

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

    @ApiModelProperty("禁言截止时间")
    private Date noSpeakDate;

}
