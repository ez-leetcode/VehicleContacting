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
@ApiModel(description = "热点图片类")
public class HotDiscussMsg {

    @ApiModelProperty("热点帖子编号1")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long number1;

    @ApiModelProperty("热点帖子图片1")
    private String photo1;

    @ApiModelProperty("热点帖子标题1")
    private String title1;

    @ApiModelProperty("热点帖子编号2")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long number2;

    @ApiModelProperty("热点帖子图片2")
    private String photo2;

    @ApiModelProperty("热点帖子标题2")
    private String title2;

    @ApiModelProperty("热点帖子编号3")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long number3;

    @ApiModelProperty("热点帖子图片1")
    private String photo3;

    @ApiModelProperty("热点帖子标题1")
    private String title3;

}
