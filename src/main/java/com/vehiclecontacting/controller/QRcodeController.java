package com.vehiclecontacting.controller;


import com.vehiclecontacting.service.QRcodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Api(tags = "动态二维码生成类",protocols = "https")
@Slf4j
@RestController
public class QRcodeController {

    @Autowired
    private QRcodeService qRcodeService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取用户个人二维码",notes = "会返回一个二维码")
    @GetMapping("/userQRcode")
    public void getUserQRcode(@RequestParam("id") Long id, HttpServletResponse response){
        log.info("正在获取用户个人二维码，id：" + id);
        try {
            qRcodeService.getPersonQRcode(id,response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "goodsName",value = "商品名",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "price",value = "价格",required = true,dataType = "double",paramType = "query")
    })
    @ApiOperation(value = "获取支付订单二维码",notes = "也是返回一个二维码，用户扫描二维码即可")
    @GetMapping("/alipayCode")
    public void getAlipayCode(@RequestParam("id") Long id,@RequestParam("goodsName") String goodsName,
                              @RequestParam("price") Double price,HttpServletResponse response){
        log.info("正在获取支付订单二维码，id：" + id + " goodsName：" + goodsName + " price：" + price);
        try {
            qRcodeService.getAlipayQRcode(id,goodsName,price,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
