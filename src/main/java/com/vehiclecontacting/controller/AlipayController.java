package com.vehiclecontacting.controller;


import com.vehiclecontacting.service.AlipayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "支付宝控制类")
@RestController
public class AlipayController {


    @Autowired
    private AlipayService alipayService;



    @ApiImplicitParams({
            @ApiImplicitParam(name = "price",value = "价格",required = true,dataType = "double",paramType = "query"),
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "支付订单",notes = "会返回一段js代码跳转支付宝支付页面")
    @GetMapping("/payBill")
    public void payBill(){

    }





}
