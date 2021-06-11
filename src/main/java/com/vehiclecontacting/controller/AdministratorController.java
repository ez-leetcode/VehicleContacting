package com.vehiclecontacting.controller;


import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.Result;
import com.vehiclecontacting.service.AdministratorService;
import com.vehiclecontacting.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@Api(tags = "管理员管理类",protocols = "https")
@RequestMapping("/admin")
@Slf4j
@RestController
public class AdministratorController {

    @Autowired
    private AdministratorService administratorService;


    @Secured("ROLE_USER")
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


    @Secured("ROLE_USER")
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


    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "被封禁用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "minutes",value = "封禁时间（按分钟为单位吧）",required = true,dataType = "int",paramType = "query")
    })
    @ApiOperation(value = "封禁用户",notes = "existWrong：用户不存在 success：成功")
    @PostMapping("/frozeUser")
    public Result<JSONObject> frozeUser(@RequestParam("id") Long id,@RequestParam("minutes") Integer minutes){
        log.info("正在封禁用户，id：" + id + " minutes：" + minutes);
        return ResultUtils.getResult(new JSONObject(),administratorService.frozeUser(id,minutes));
    }


    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "被解封人的id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "解封用户",notes = "existWrong：用户不存在 repeatWrong：用户已被解封（可能是重复请求） success：成功")
    @PostMapping("/reopenUser")
    private Result<JSONObject> reopenUser(@RequestParam("id") Long id){
        log.info("正在解封用户，id：" + id);
        return ResultUtils.getResult(new JSONObject(),administratorService.reopenUser(id));
    }

    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "number",value = "帖子编号",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "reason",value = "删帖原因（用于通知）",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "删除帖子",notes = "existWrong：帖子不存在 success：成功")
    @DeleteMapping("/discuss")
    public Result<JSONObject> deleteDiscuss(@RequestParam("number") Long number,@RequestParam("reason") String reason){
        log.info("正在管理员删帖，number：" + number + " reason：" + reason);
        return ResultUtils.getResult(new JSONObject(),administratorService.deleteDiscuss(number,reason));
    }


    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "被禁言用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "hours",value = "禁言时间（可以给永封按钮，给封个几万个小时的参数就行了，87600小时=10年）",required = true,dataType = "int",paramType = "query")
    })
    @ApiOperation(value = "管理员禁言别人",notes = "已经被禁言的，还可以重新改禁言时间，就不repeatWrong了，success：成功")
    @PostMapping("/frozeSpeak")
    public Result<JSONObject> frozeSpeak(@RequestParam("id") Long id,@RequestParam("hours") Integer hours){
        log.info("正在管理员禁言别人，id：" + id + " hours：" + hours);
        return ResultUtils.getResult(new JSONObject(),administratorService.frozeSpeak(id,hours));
    }


    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取被封禁用户",notes = "success：成功 （返回json frozenUserList：封禁用户信息列表 pages：页面总数 counts：数据总量）")
    @GetMapping("/frozenList")
    public Result<JSONObject> getFrozenList(@RequestParam("cnt") Long cnt,@RequestParam("page") Long page){
        log.info("正在获取封禁用户，cnt：" + cnt + " page：" + page);
        return ResultUtils.getResult(administratorService.getFrozenUser(cnt,page),"success");
    }

    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "isPass",value = "是否通过（0：给没通过的 1：给通过的 2：给拒绝的 3：给全部）",required = true,dataType = "int",paramType = "query"),
            @ApiImplicitParam(name = "keyword",value = "搜索关键词（不想做就不提交这个参数）",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "获取用户申诉车牌列表",notes = "success：成功  返回json：complainVehicleList：申诉车牌列表  pages：页面总数  counts：数据总量")
    @GetMapping("/complainVehicle")
    public Result<JSONObject> getComplainVehicle(@RequestParam("id") Long id,@RequestParam("cnt") Long cnt,
                                                 @RequestParam("page") Long page,@RequestParam("isPass") Integer isPass,
                                                 @RequestParam(value = "keyword",required = false) String keyword){
        log.info("正在获取用户申诉车牌列表，id：" + id + " cnt：" + cnt + " page：" + page + " isPass：" + isPass + " keyword：" + keyword);
        return ResultUtils.getResult(administratorService.getComplainVehicleList(id,cnt,page,isPass,keyword),"success");
    }


    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "number",value = "车辆申诉编号",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "isPass",value = "是否通过（1：通过 2：拒绝）",required = true,dataType = "int",paramType = "query"),
            @ApiImplicitParam(name = "backReason",value = "退回原因",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "审核用户车辆申诉（会有推送）",notes = "repeatWrong：已经被审核过 existWrong：申诉不存在 success：成功")
    @PostMapping("/complainVehicle")
    public Result<JSONObject> judgeComplainVehicle(@RequestParam("id") Long id,@RequestParam("number") Long number,
                                                   @RequestParam("isPass") Integer isPass,
                                                   @RequestParam(value = "backReason",required = false) String backReason){
        log.info("正在审核用户车辆申诉，id：" + id + " number：" + number + " isPass：" + isPass + " backReason：" + backReason);
        return ResultUtils.getResult(new JSONObject(),administratorService.judgeComplainVehicle(id,number,isPass,backReason));
    }


}