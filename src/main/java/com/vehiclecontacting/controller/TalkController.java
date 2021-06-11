package com.vehiclecontacting.controller;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.Result;
import com.vehiclecontacting.service.TalkService;
import com.vehiclecontacting.utils.ResultUtils;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "聊天管理类",protocols = "https")
@Slf4j
@RestController
public class TalkController {


    @Autowired
    private TalkService talkService;


    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取用户的聊天列表",notes = "success：成功 （返回json talkMsgList：聊天列表 pages：页面总数 counts：数据量）")
    @GetMapping("/talkList")
    public Result<JSONObject> getTalkList(@RequestParam("id") Long id,@RequestParam("cnt") Long cnt,@RequestParam("page") Long page){
        log.info("正在获取用户聊天列表，id：" + id + " cnt：" + cnt + " page：" + page);
        return ResultUtils.getResult(talkService.getTalkList(id,cnt,page),"success");
    }

    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "全部标为已读",notes = "success：成功")
    @PostMapping("/allRead")
    public Result<JSONObject> allRead(@RequestParam("id") Long id){
        log.info("正在标为全部已读，id：" + id);
        return ResultUtils.getResult(new JSONObject(),talkService.allRead(id));
    }

    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "对方id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "标记和一个用户的全部聊天为已读",notes = "existWrong：聊天不存在 success：成功")
    @PostMapping("/isRead")
    public Result<JSONObject> isRead(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId){
        log.info("正在标记和一个用户的全部聊天为已读，fromId：" + fromId + " toId：" + toId);
        return ResultUtils.getResult(new JSONObject(),talkService.isRead(fromId,toId));
    }

    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "被删除用户id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "暂时删除该用户在列表的呈现",notes = "existWrong：该消息不存在（可能时重复请求） success：成功")
    @DeleteMapping("/talk")
    public Result<JSONObject> deleteTalk(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId){
        log.info("正在删除用户在列表呈现");
        return ResultUtils.getResult(new JSONObject(),talkService.deletedTalk(fromId,toId));
    }

    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "对方id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "numbers",value = "聊天消息编号",required = true,allowMultiple = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "批量删除用户聊天信息",notes = "userWrong：聊天消息不匹配 existWrong：消息不存在 success：成功")
    @DeleteMapping("/talkMsg")
    public Result<JSONObject> deleteTalkMsg(@RequestParam("fromId") Long fromId, @RequestParam("toId") Long toId,
                                            @RequestParam("numbers") List<Long> numbers){
        log.info("正在批量删除用户聊天信息，fromId：" + fromId + " toId：" + toId + " numbers：" + numbers.toString());
        return ResultUtils.getResult(new JSONObject(),talkService.deleteTalkMsg(fromId,toId,numbers));
    }

    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "对方id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取用户一对一聊天列表（同时也会全部标记已读）",notes = "success：成功  （返回json talkList：聊天列表 counts：数据总量 pages：页面总数）")
    @GetMapping("/talk")
    public Result<JSONObject> getP2PTalkList(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId,
                                             @RequestParam("cnt") Long cnt,@RequestParam("page") Long page){
        log.info("正在获取用户一对一聊天列表，fromId：" + fromId + " toId：" + toId + " cnt：" + cnt + " page：" + page);
        return ResultUtils.getResult(talkService.getP2PTalkList(fromId,toId,page,cnt),"success");
    }

    @Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "对方id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取用户是否在线，昵称等信息（私聊上方）",notes = "success：成功  返回json userMsg")
    @GetMapping("/talkHead")
    public Result<JSONObject> judgeTalkHead(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId){
        log.info("正在获取用户是否在线，昵称等信息，fromId：" + fromId + " toId：" + toId);
        return ResultUtils.getResult(talkService.judgeTalkHead(fromId,toId),"success");
    }


}