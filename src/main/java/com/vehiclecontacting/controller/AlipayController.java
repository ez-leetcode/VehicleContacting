package com.vehiclecontacting.controller;


import com.vehiclecontacting.service.AlipayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Api(tags = "支付宝控制类")
@RestController
public class AlipayController {


    @Autowired
    private AlipayService alipayService;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "price",value = "价格",required = true,dataType = "double",paramType = "query"),
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "goodsName",value = "商品名称",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "支付订单",notes = "会返回一段js代码跳转支付宝支付页面")
    @GetMapping("/payBill")
    public void payBill(HttpServletResponse response, HttpServletRequest request,
                        @RequestParam("price") Double price, @RequestParam("id") Long id,
                        @RequestParam("goodsName") String goodsName){
        log.info("正在生成订单，price：" + price + " id：" + id + " goodsName：" + goodsName);
        try{
            alipayService.aliPay(response,request,goodsName,price,id);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "回调判断是否付款",notes = "这个接口付完款，支付宝会调用回调，自动改变订单状态，不用你调用哈~")
    @PostMapping("/notifyBill")
    public void notifyBill(HttpServletRequest request){
        log.info("正在回调付款信息");
        log.info(request.toString());
        try {
            alipayService.notifyBill(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("回调成功");
    }


}
