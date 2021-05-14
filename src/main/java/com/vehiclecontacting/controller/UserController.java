package com.vehiclecontacting.controller;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.Result;
import com.vehiclecontacting.service.UserService;
import com.vehiclecontacting.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "用户控制类",protocols = "https")
@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserService userService;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone",value = "电话",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "code",value = "验证码",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "用户手机短信验证码注册",notes = "phoneWrong：电话号码已被绑定 existWrong：手机号不存在（验证码发送错误） repeatWrong：重复获取太多次 success：成功")
    @PostMapping("/register")
    public Result<JSONObject> register(@RequestParam("phone") String phone,@RequestParam("code") String code){
        log.info("用户正在用手机注册，电话：" + phone + " 验证码：" + code);
        return ResultUtils.getResult(new JSONObject(), userService.register(phone,code));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone",value = "电话",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "type",value = "哪种验证码（1.注册 2.修改密码 3.找回密码）",required = true,dataType = "int",paramType = "query")
    })
    @ApiOperation(value = "发送短信验证码")
    @PostMapping("/code")
    public Result<JSONObject> sendCode(@RequestParam("phone") String phone,@RequestParam("type") Integer type){
        log.info("正在发送短信验证码，电话：" + phone + " 类型：" + type);
        return ResultUtils.getResult(new JSONObject(), userService.sendCode(phone,type));
    }


    //@Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "photo",value = "头像文件",required = true,dataType = "file",paramType = "query"),
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "用户上传头像（需要用户角色）",notes = "existWrong：用户不存在 fileWrong：文件为空 typeWrong：上传格式错误 success：成功，成功后json会带头像的url")
    @PostMapping("/userPhoto")
    public Result<JSONObject> uploadPhoto(@RequestParam("photo") MultipartFile file, @RequestParam("id") String id) {
        JSONObject jsonObject = new JSONObject();
        Result<JSONObject> result;
        log.info("正在上传用户头像，id：" + id);
        String status = userService.uploadPhoto(file, id);
        if(status.length() > 12){
            //存的是url
            jsonObject.put("url",status);
            result = ResultUtils.getResult(jsonObject,"success");
        }else{
            result = ResultUtils.getResult(jsonObject,status);
        }
        return result;
    }



}
