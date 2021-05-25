package com.vehiclecontacting.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@ApiModel(description = "用户反馈类")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Feedback {

    @ApiModelProperty("用户id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("主题")
    private String title;

    @ApiModelProperty("内容")
    private String description;

    @ApiModelProperty("是否已读")
    private Integer isRead;

    @ApiModelProperty("创建时间")
    private Date createTime;

}
