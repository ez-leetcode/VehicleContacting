package com.vehiclecontacting.controller;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.Result;
import com.vehiclecontacting.service.VehicleService;
import com.vehiclecontacting.utils.ResultUtils;
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
import org.springframework.web.multipart.MultipartFile;


@Api(tags = "车辆信息管理类",protocols = "https")
@Slf4j
@RestController
public class VehicleController {


    @Autowired
    private VehicleService vehicleService;



    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "type",value = "车辆类型（等下我们商量一下）",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "license",value = "牌照",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "licensePhoto",value = "牌照照片url",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "vehiclePhoto1",value = "车辆图片1",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "vehiclePhoto2",value = "车辆图片2",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "vehiclePhoto3",value = "车辆图片3",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "description",value = "描述（250字以内）",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "生成车辆信息",notes = "repeatWrong：车牌号已被申请 existWrong：用户不存在 amountWrong：用户上传车辆超过4个 success：成功")
    @PostMapping("/vehicle")
    public Result<JSONObject> generateVehicle(@RequestParam("id") Long id,@RequestParam("type") Integer type,@RequestParam("license") String license,
                                              @RequestParam("licensePhoto") String licensePhoto,@RequestParam("vehiclePhoto1") String vehiclePhoto1,
                                              @RequestParam("vehiclePhoto2") String vehiclePhoto2,@RequestParam("vehiclePhoto3") String vehiclePhoto3,
                                              @RequestParam("description") String description){
        log.info("正在生成车辆信息，id：" + id + " type：" + type + " license：" + license + " licensePhoto：" + licensePhoto + " vehiclePhoto1：" + vehiclePhoto1 +
                 " vehiclePhoto2：" + vehiclePhoto2 + " vehiclePhoto3：" + vehiclePhoto3 + " description：" + description);
        return ResultUtils.getResult(new JSONObject(),vehicleService.generateVehicle(id,type,license,licensePhoto,vehiclePhoto1,vehiclePhoto2,vehiclePhoto3,description));
    }



    @ApiImplicitParams({
            @ApiImplicitParam(name = "photo",value = "图片文件",required = true,dataType = "file",paramType = "body"),
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "车辆或车牌图片上传",notes = "existWrong：用户不存在 fileWrong：文件为空 typeWrong：上传格式错误 success：成功，成功后返回json：url（图片url）")
    @PostMapping("/vehiclePhoto")
    public Result<JSONObject> vehiclePhotoUpload(@RequestParam("id") String id, @RequestParam("photo") MultipartFile file){
        String realId = id.substring(1,id.length() - 1);
        log.info("正在上传车辆牌照，id：" + realId);
        String url = vehicleService.vehiclePhotoUpload(file,realId);
        if(url.length() < 12){
            return ResultUtils.getResult(new JSONObject(),url);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url",url);
        return ResultUtils.getResult(jsonObject,"success");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "type",value = "车辆类型",dataType = "int",paramType = "query"),
            @ApiImplicitParam(name = "keyword",value = "搜索关键词",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "主页面获取车辆信息",notes = "success：成功 （返回json vehicleList（车辆相关信息列表） pages：页面总数 counts：数据总量）")
    @GetMapping("/searchVehicle")
    public Result<JSONObject> searchVehicle(@RequestParam("cnt") Long cnt,@RequestParam("page") Long page,
                                            @RequestParam(value = "keyword",required = false) String keyword,
                                            @RequestParam(value = "type",required = false) Integer type){
        log.info("正在主页面获取车辆信息，keyword：" + keyword + " cnt：" + cnt + " page：" + page + " type：" + type);
        return ResultUtils.getResult(vehicleService.searchVehicle(page,cnt,keyword,type),"success");
    }




}