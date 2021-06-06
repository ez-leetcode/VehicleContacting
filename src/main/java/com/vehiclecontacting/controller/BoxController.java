package com.vehiclecontacting.controller;


import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.Result;
import com.vehiclecontacting.service.BoxService;
import com.vehiclecontacting.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(tags = "消息盒子管理类")
@RestController
public class BoxController {

    @Autowired
    private BoxService boxService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "keyword",value = "搜索关键词",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "获取消息盒子内容（会更新已读）",notes = "success：成功  （返回json  messageList：消息列表 pages：页面总数 counts：数据总量）")
    @GetMapping("/allBox")
    public Result<JSONObject> getAllBox(@RequestParam("id") Long id,@RequestParam("cnt") Long cnt,
                                        @RequestParam("page") Long page,@RequestParam(value = "keyword",required = false) String keyword){
        log.info("正在获取消息盒子内容，id：" + id + " cnt：" + cnt + " page：" + page + " keyword：" + keyword);
        return ResultUtils.getResult(boxService.getAllBox(id,cnt,page,keyword),"success");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "把消息盒子内容全部设置为已读",notes = "success：成功")
    @PostMapping("/boxAllRead")
    public Result<JSONObject> boxAllRead(@RequestParam("id") Long id){
        log.info("正在把消息盒子内容全部设为已读，id：" + id);
        return ResultUtils.getResult(new JSONObject(),boxService.readAllBox(id));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "numbers",value = "消息编号",required = true,allowMultiple = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "批量删除消息盒子的消息",notes = "existWrong：消息不存在 success：成功")
    @DeleteMapping("/boxMessage")
    public Result<JSONObject> deleteBoxMessage(@RequestParam("id") Long id, @RequestParam("numbers") List<Long> numbers){
        log.info("正在批量删除消息盒子的消息，id：" + id + " numbers：" + numbers.toString());
        return ResultUtils.getResult(new JSONObject(),boxService.deleteBoxMessage(id,numbers));
    }


}