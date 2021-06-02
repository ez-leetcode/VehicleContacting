package com.vehiclecontacting.msg;


import io.swagger.annotations.ApiImplicitParam;
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
@ApiModel(description = "邮件消息实体类")
public class MailMsg {

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("验证码")
    private String yzm;

    @ApiModelProperty("用途")
    private String function;

}
