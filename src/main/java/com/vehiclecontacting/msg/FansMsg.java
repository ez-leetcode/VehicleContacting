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
@ApiModel(description = "粉丝消息类")
public class FansMsg {

    @ApiModelProperty("粉丝id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("粉丝昵称")
    private String username;

    @ApiModelProperty("粉丝性别")
    private String sex;

    @ApiModelProperty("粉丝vip")
    private Integer vip;

    @ApiModelProperty("粉丝头像")
    private String photo;

    @ApiModelProperty("粉丝自我描述")
    private String introduction;

    @ApiModelProperty("粉丝关注时间")
    private Date createTime;

}
