package com.vehiclecontacting.controller;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.Result;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.service.SmsService;
import com.vehiclecontacting.service.UserService;
import com.vehiclecontacting.utils.RedisUtils;
import com.vehiclecontacting.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Random;

@Api(tags = "用户控制类",protocols = "https")
@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private RedisUtils redisUtils;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone",value = "电话",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "code",value = "验证码",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "password",value = "密码",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "用户手机短信验证码注册",notes = "existWrong：验证码不存在或已过期 codeWrong：验证码错误 repeatWrong：该手机已被绑定 success：成功")
    @PostMapping("/register")
    public Result<JSONObject> register(@RequestParam("phone") String phone,@RequestParam("code") String code,
                                       @RequestParam("password") String password){
        log.info("用户正在用手机注册，电话：" + phone + " 验证码：" + code);
        return ResultUtils.getResult(new JSONObject(), userService.register(phone,code,password));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone",value = "电话",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "code",value = "验证码",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "用户手机验证码登录",notes = "codeExistWrong：验证码不存在或已失效 existWrong：账号不存在（用户没注册）" +
            " codeWrong：验证码错误（验证码可以不区分大小写） frozenWrong：用户已被封号（返回json带frozenDate：封号截止时间） success：成功（返回json带token：token令牌）")
    @PostMapping("/loginByCode")
    public Result<JSONObject> loginByCode(@RequestParam("phone") String phone,@RequestParam("code") String code){
        log.info("用户正在使用验证码登录,电话：" + phone + " 验证码：" + code);
        String status = userService.loginByCode(phone,code);
        if(status.equals("existWrong") || status.equals("codeWrong") || status.equals("codeExistWrong")){
            return ResultUtils.getResult(new JSONObject(),status);
        }
        if(!status.equals("success")){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("frozenDate",status);
            return ResultUtils.getResult(jsonObject,"frozenWrong");
        }
        //可能要加入websocket的功能
        JSONObject jsonObject = userService.generateToken(phone);
        return ResultUtils.getResult(jsonObject,"success");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone",value = "电话",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "code",value = "验证码",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "oldPassword",value = "旧密码",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "newPassword",value = "新密码",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "用户改密码",notes = "existWrong：用户不存在 oldPasswordWrong：旧密码错误 codeExistWrong：验证码不存在或已过期 codeWrong：验证码错误 success：成功")
    @PostMapping("/changePassword")
    public Result<JSONObject> changePassword(@RequestParam("phone") String phone, @RequestParam("code") String code,
                                             @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword){
        log.info("用户正在修改密码，手机：" + phone + " 验证码：" + code + " 旧密码：" + oldPassword + " 新密码：" + newPassword);
        return ResultUtils.getResult(new JSONObject(),userService.changePassword(phone,code,oldPassword,newPassword));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone",value = "电话",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "code",value = "验证码",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "newPassword",value = "新密码",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "用户找回密码",notes = "existWrong：用户不存在 codeExistWrong：验证码不存在或已过期 codeWrong：验证码错误 success：成功")
    @PostMapping("/findPassword")
    public Result<JSONObject> findPassword(@RequestParam("phone") String phone,@RequestParam("code") String code,
                                           @RequestParam("newPassword") String newPassword){
        log.info("用户正在找回密码，手机：" + phone + " 验证码：" + code + " 新密码：" + newPassword);
        return ResultUtils.getResult(new JSONObject(),userService.findPassword(phone,code,newPassword));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone",value = "电话",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "type",value = "哪种验证码（1.注册 2.修改密码 3.找回密码 4.登录）",required = true,dataType = "int",paramType = "query")
    })
    @ApiOperation(value = "发送短信验证码",notes = "repeatWrong：获取验证码次数过多，existWrong：手机号不存在（验证码发送错误） success：成功")
    @PostMapping("/code")
    public Result<JSONObject> sendCode(@RequestParam("phone") String phone,@RequestParam("type") Integer type){
        log.info("正在发送短信验证码，电话：" + phone + " 类型：" + type);
        String status = userService.judgeCode(phone,type);
        if(!status.equals("success")){
            return ResultUtils.getResult(new JSONObject(),status);
        }
        Random random = new Random();
        int yzm = random.nextInt(999999);
        String code = Integer.toString(yzm);
        boolean isSend = smsService.sendSms(phone,code,type);
        if(isSend){
            //成功发送验证码
            redisUtils.saveByMinutesTime(type + "_" + phone,code,15);
            String sendCounts = redisUtils.getValue("sendCode_" + phone);
            int cnt = 0;
            if(sendCounts != null){
                cnt = Integer.parseInt(sendCounts);
            }
            cnt ++;
            //保存近期发送时间
            redisUtils.saveByHoursTime("sendCode_",String.valueOf(cnt),2);
            return ResultUtils.getResult(new JSONObject(),"success");
        }
        return ResultUtils.getResult(new JSONObject(),"existWrong");
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "phone",value = "用户电话",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "获取用户信息（需要用户角色）",notes = "existWrong：用户不存在 success：成功（返回json带user：用户信息）")
    @GetMapping("/user")
    public Result<JSONObject> getUser(@RequestParam(value = "id",required = false) Long id,
                                      @RequestParam(value = "phone",required = false) String phone){
        log.info("正在获取用户信息，用户id");
        JSONObject jsonObject = new JSONObject();
        User user = userService.getUser(id,phone);
        if(user == null){
            return ResultUtils.getResult(jsonObject,"existWrong");
        }
        jsonObject.put("user",user);
        return ResultUtils.getResult(jsonObject,"success");
    }


    //@Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "username",value = "用户昵称",dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "sex",value = "性别",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "修改用户信息（需要用户角色）",notes = "existWrong：用户不存在 repeatWrong：用户没有修改信息（可能是重复请求或者用户写相同的信息保存）success：成功")
    @PatchMapping("/user")
    public Result<JSONObject> patchUser(@RequestParam("id") Long id,@RequestParam(value = "username",required = false) String username,
                                        @RequestParam(value = "sex",required = false) String sex){
        log.info("正在修改用户信息");
        return ResultUtils.getResult(new JSONObject(),userService.patchUser(id,username,sex));
    }


    //@Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "photo",value = "头像文件",required = true,dataType = "file",paramType = "body"),
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "用户上传头像（需要用户角色）",notes = "existWrong：用户不存在 fileWrong：文件为空 typeWrong：上传格式错误 success：成功，成功后返回json：url（头像url）")
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


    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "关注者id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "被关注者id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "添加关注",notes = "existWrong：用户不存在 repeatWrong：已经关注了（可能是重复请求） success：成功")
    @PostMapping("/fans")
    public Result<JSONObject> addFans(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId){
        log.info("正在添加关注，关注者：" + fromId + " 被关注者：" + toId);
        return ResultUtils.getResult(new JSONObject(),userService.addFans(fromId,toId));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "关注者id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "被关注者id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "取消关注",notes = "existWrong：用户不存在 repeatWrong：已经取消关注了/或者没有关注（可能是重复请求） success：成功")
    @DeleteMapping("/fans")
    public Result<JSONObject> removeFans(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId){
        log.info("正在移除关注，关注者：" + fromId + " 被关注者：" + toId);
        return ResultUtils.getResult(new JSONObject(),userService.removeFans(fromId,toId));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "keyword",value = "搜索关键词",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "获取用户粉丝列表",notes = "success：成功 ")
    @GetMapping("/fans")
    public Result<JSONObject> getFans(@RequestParam("id") Long id,@RequestParam("cnt") Long cnt,
                                      @RequestParam("page") Long page,@RequestParam(value = "keyword",required = false) String keyword){
        log.info("正在获取用户粉丝列表，id：" + id + " cnt：" + cnt + " page：" + page);
        return ResultUtils.getResult(userService.getFans(id,cnt,page,keyword),"success");
    }





}