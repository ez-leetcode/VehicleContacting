package com.vehiclecontacting.msg;


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
@ApiModel(description = "封禁用户信息类")
public class FrozenUserMsg {

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("昵称")
    private String username;

    @ApiModelProperty("头像url")
    private String photo;

    @ApiModelProperty("vip")
    private Integer vip;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("封禁时间")
    private Date frozenDate;

    @ApiModelProperty("重开时间")
    private Date reopenDate;

}
