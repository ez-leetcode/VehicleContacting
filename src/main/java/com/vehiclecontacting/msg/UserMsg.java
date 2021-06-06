package com.vehiclecontacting.msg;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@ApiModel(description = "用户信息类")
public class UserMsg {

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("昵称")
    private String username;

    @ApiModelProperty("头像")
    private String photo;

    @ApiModelProperty("性别")
    private String sex;

    @ApiModelProperty("vip")
    private Integer vip;

    @ApiModelProperty("自我介绍")
    private String introduction;

}