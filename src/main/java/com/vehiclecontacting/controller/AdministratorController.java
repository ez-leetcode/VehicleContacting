package com.vehiclecontacting.controller;


import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.Result;
import com.vehiclecontacting.service.AdministratorService;
import com.vehiclecontacting.utils.RedisUtils;
import com.vehiclecontacting.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "管理员管理类",protocols = "https")
@RequestMapping("/admin")
@Slf4j
@RestController
public class AdministratorController {

    @Autowired
    private AdministratorService administratorService;

    @Autowired
    private RedisUtils redisUtils;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "license",value = "车牌号",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "isPass",value = "是否通过（1：通过 2：审核不通过，多带一个参数原因）",required = true,dataType = "int",paramType = "query"),
            @ApiImplicitParam(name = "reason",value = "不通过说明原因",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "审核车辆信息",notes = "existWrong：车辆信息不存在 repeatWrong：该商品已被审核（重复提交或者被别的管理员审核了） success：成功")
    @PostMapping("/judgeVehicle")
    public Result<JSONObject> judgeVehicle(@RequestParam("license") String license,@RequestParam("isPass") Integer isPass,
                                           @RequestParam("reason") String reason){
        log.info("正在审核车牌，license：" + license + " isPass：" + isPass + " reason：" + reason);
        return ResultUtils.getResult(new JSONObject(),administratorService.judgeVehicle(license,isPass,reason));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "keyword",value = "关键词",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "获取需要审核的车辆列表",notes = "success：成功 （返回 vehicleList：需要审核车辆大致信息（只有一部分信息，需要点进去看，有另外一个接口给你））")
    @GetMapping("/vehicleList")
    public Result<JSONObject> getVehicleList(@RequestParam("cnt") Long cnt,@RequestParam("page") Long page,
                                             @RequestParam(value = "keyword",required = false) String keyword){
        log.info("正在获取需要审核的车辆列表，cnt：" + cnt + " page：" + page + " keyword：" + keyword);
        return ResultUtils.getResult(administratorService.getVehicleList(cnt,page,keyword),"success");
    }


}
