package com.vehiclecontacting.controller;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.Result;
import com.vehiclecontacting.service.TalkService;
import com.vehiclecontacting.utils.ResultUtils;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "聊天管理类",protocols = "https")
@Slf4j
@RestController
public class TalkController {


    @Autowired
    private TalkService talkService;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取用户的聊天列表（还在施工）",notes = "success：成功 （返回json） talkList：聊天列表 pages：页面总数 counts：数据量")
    @GetMapping("/talkList")
    public Result<JSONObject> getTalkList(@RequestParam("id") Long id,@RequestParam("cnt") Long cnt,@RequestParam("page") Long page){
        log.info("正在获取用户聊天列表，id：" + id + " cnt：" + cnt + " page：" + page);
        return ResultUtils.getResult(talkService.getTalkList(id,cnt,page),"success");
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "全部标为已读",notes = "success：成功")
    @PostMapping("/allRead")
    public Result<JSONObject> allRead(@RequestParam("id") Long id){
        log.info("正在标为全部已读，id：" + id);
        return ResultUtils.getResult(new JSONObject(),talkService.allRead(id));
    }

    /*
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "被删除用户id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "暂时删除该用户在列表的呈现",notes = "existWrong：该消息不存在（可能时重复请求） success：成功")
    @DeleteMapping("/talk")
    public Result<JSONObject> deleteTalk(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId){
        log.info("正在删除用户在列表呈现");
    }

     */



}
