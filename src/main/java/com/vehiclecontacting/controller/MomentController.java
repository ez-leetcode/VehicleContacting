package com.vehiclecontacting.controller;


import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.Result;
import com.vehiclecontacting.service.MomentService;
import com.vehiclecontacting.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "动态管理类",protocols = "https")
@Slf4j
@RestController
public class MomentController {

    @Autowired
    private MomentService momentService;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "photo",value = "图片",required = true,dataType = "file",paramType = "body")
    })
    @ApiOperation(value = "动态图片上传",notes = "fileWrong：文件为空 typeWrong：上传格式错误 success：成功 （返回json url（图片路径））")
    @PostMapping("/momentPhoto")
    public Result<JSONObject> momentPhotoUpload(@RequestParam("id") Long id, @RequestParam("photo") MultipartFile file){
        log.info("正在上传动态图片，id：" + id);
        String url = momentService.momentPhotoUpload(file,id);
        if(url.length() < 12){
            return ResultUtils.getResult(new JSONObject(),url);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url",url);
        return ResultUtils.getResult(jsonObject,"success");
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "description",value = "内容",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "photo1",value = "图片1",dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "photo2",value = "图片2",dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "photo3",value = "图片3",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "生成动态",notes = "existWrong：用户不存在 repeatWrong：24小时内发动态超过10次  success：成功")
    @PostMapping("/moment")
    public Result<JSONObject> generateComment(@RequestParam("id") Long id,@RequestParam("description") String description,
                                              @RequestParam("photo1") String photo1,@RequestParam("photo2") String photo2,
                                              @RequestParam("photo3") String photo3){
        log.info("正在生成动态，id：" + id + " description：" + description);
        return ResultUtils.getResult(new JSONObject(),momentService.generateMoment(id,description,photo1,photo2,photo3));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "number",value = "动态编号",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "删除动态（动态主人）",notes = "userWrong：用户不匹配或不存在（只有动态主人或者管理员才能删除） existWrong：动态不存在（可能是重复请求） success：成功")
    @DeleteMapping("/moment")
    public Result<JSONObject> deleteMoment(@RequestParam("id") Long id,@RequestParam("number") Long number){
        log.info("正在删除动态，id：" + id + " number：" + number);
        return ResultUtils.getResult(new JSONObject(), momentService.deleteMoment(id,number));
    }


}
