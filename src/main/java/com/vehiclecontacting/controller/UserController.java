package com.vehiclecontacting.controller;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.config.RabbitmqProductConfig;
import com.vehiclecontacting.pojo.Result;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.service.MailService;
import com.vehiclecontacting.service.SmsService;
import com.vehiclecontacting.service.UserService;
import com.vehiclecontacting.utils.RedisUtils;
import com.vehiclecontacting.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Api(tags = "用户管理类",protocols = "https")
@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private MailService mailService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RabbitmqProductConfig rabbitmqProductConfig;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone",value = "电话",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "code",value = "验证码",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "password",value = "密码",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "用户手机短信验证码注册（暂时不用啦~）",notes = "existWrong：验证码不存在或已过期 codeWrong：验证码错误 repeatWrong：该手机已被绑定 success：成功")
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
    @ApiOperation(value = "用户手机验证码登录",notes = "codeExistWrong：验证码不存在或已失效 " +
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
    @ApiOperation(value = "用户改密码（暂时不用啦~）",notes = "existWrong：用户不存在 oldPasswordWrong：旧密码错误 codeExistWrong：验证码不存在或已过期 codeWrong：验证码错误 success：成功")
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
            @ApiImplicitParam(name = "sex",value = "性别",dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "introduction",value = "自我介绍",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "修改用户信息（需要用户角色）",notes = "existWrong：用户不存在 repeatWrong：用户没有修改信息（可能是重复请求或者用户写相同的信息保存）success：成功")
    @PatchMapping("/user")
    public Result<JSONObject> patchUser(@RequestParam("id") Long id,@RequestParam(value = "username",required = false) String username,
                                        @RequestParam(value = "sex",required = false) String sex,
                                        @RequestParam(value = "introduction",required = false) String introduction){
        log.info("正在修改用户信息");
        return ResultUtils.getResult(new JSONObject(),userService.patchUser(id,username,sex,introduction));
    }


    //@Secured("ROLE_USER")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "photo",value = "头像文件",required = true,dataType = "file",paramType = "body"),
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "用户上传头像（需要用户角色）",notes = "existWrong：用户不存在 fileWrong：文件为空 typeWrong：上传格式错误 success：成功 成功后返回json：url（头像url）")
    @PostMapping("/userPhoto")
    public Result<JSONObject> uploadPhoto(@RequestParam("photo") MultipartFile file, @RequestParam("id") String id) {
        String realId = id.substring(1,id.length() -1);
        JSONObject jsonObject = new JSONObject();
        Result<JSONObject> result;
        log.info("正在上传用户头像，id：" + realId);
        String status = userService.uploadPhoto(file, realId);
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
    @ApiOperation(value = "获取用户粉丝列表",notes = "success：成功 （返回json fansList（粉丝信息列表） pages（页面总数） counts（数据总量））")
    @GetMapping("/fans")
    public Result<JSONObject> getFans(@RequestParam("id") Long id,@RequestParam("cnt") Long cnt,
                                      @RequestParam("page") Long page,@RequestParam(value = "keyword",required = false) String keyword){
        log.info("正在获取用户粉丝列表，id：" + id + " cnt：" + cnt + " page：" + page);
        return ResultUtils.getResult(userService.getFans(id,cnt,page,keyword),"success");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "keyword",value = "搜索关键词",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "获取用户关注列表",notes = "success：成功 （返回json followList（关注信息列表） pages（页面总数） counts（数据总量））")
    @GetMapping("/follow")
    public Result<JSONObject> getFollow(@RequestParam("id") Long id,@RequestParam("cnt") Long cnt,
                                        @RequestParam("page") Long page,@RequestParam(value = "keyword",required = false) String keyword) {
        log.info("正在获取用户关注列表，id：" + id + " cnt：" + cnt + " page：" + page);
        return ResultUtils.getResult(userService.getFollow(id,cnt,page,keyword),"success");
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "email",value = "邮箱",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "type",value = "哪种类型（1：绑定或改绑定验证码  其他待增加）",required = true,dataType = "int",paramType = "query")
    })
    @ApiOperation(value = "发送邮件验证码",notes = "repeatWrong：2小时内改绑太多次（10次） success：成功")
    @PostMapping("/emailCode")
    public Result<JSONObject> sendEmailCode(@RequestParam("email") String email,@RequestParam("type") Integer type){
        log.info("正在发送邮件验证码，email：" + email + " type：" + type);
        int cnt = 0;
        String count = redisUtils.getValue("email" + "_" + email);
        if(count != null){
            cnt = Integer.parseInt(count);
        }
        cnt ++;
        if(cnt > 10){
            log.warn("发送验证码失败，用户改绑太频繁");
            return ResultUtils.getResult(new JSONObject(),"repeatWrong");
        }
        String yzm = UUID.randomUUID().toString().substring(0,5);
        log.info("已创建验证码：" + yzm + " 邮箱：" + email);
        //验证码存入redis
        redisUtils.saveByMinutesTime("email" + type + "_" + email,yzm,15);
        //发送邮件，用rabbitmq解耦
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email",email);
        jsonObject.put("yzm",yzm);
        jsonObject.put("function","绑定邮箱");
        rabbitmqProductConfig.sendMsg(jsonObject);
        //mailService.sendEmail(email,yzm,"绑定邮箱");
        //次数加1
        redisUtils.addKeyByTime("email" + "_" + email,2);
        return ResultUtils.getResult(new JSONObject(),"success");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "code",value = "邮箱验证码",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "newEmail",value = "新邮箱",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "用户绑定邮箱或者改绑定邮箱",notes = "repeatWrong：邮箱已被他人绑定 codeExistWrong：验证码不存在或已失效 existWrong：用户不存在 codeWrong：验证码错误 success：成功")
    @PostMapping("/email")
    public Result<JSONObject> changeEmail(@RequestParam("id") Long id,@RequestParam("code") String code,
                                          @RequestParam("newEmail") String newEmail){
        log.info("正在绑定邮箱，id：" + id + " code：" + code + " newEmail：" + newEmail);
        return ResultUtils.getResult(new JSONObject(), userService.changeEmail(id,code,newEmail));
    }


    @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query")
    @ApiOperation(value = "清空历史记录",notes = "repeatWrong：历史记录已被清空（可能是重复请求） success：成功")
    @DeleteMapping("/allHistory")
    public Result<JSONObject> clearHistory(@RequestParam("id") Long id){
        log.info("正在清空历史记录，id：" + id);
        return ResultUtils.getResult(new JSONObject(), userService.clearHistory(id));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取历史记录列表（施工完了）",notes = "success：成功 （返回json historyList（历史记录列表） pages：（页面总数） counts：（数据总量））")
    @GetMapping("/history")
    public Result<JSONObject> getHistory(@RequestParam("id") Long id,@RequestParam("cnt") Long cnt,
                                         @RequestParam("page") Long page){
        log.info("正在获取用户历史记录，id：" + id + " cnt：" + cnt + " page：" + page);
        return ResultUtils.getResult(userService.getHistory(id,page,cnt),"success");
    }



    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "查看者",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "被查看者",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "查看用户里判断关注状态",notes = "success：成功 （返回status 1：未关注 2：已关注 3：已相互关注）")
    @PostMapping("/judgeFavor")
    public Result<JSONObject> judgeFavor(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId){
        log.info("正在判断用户关注状态，fromId：" + fromId + " toId：" + toId);
        Integer status = userService.judgeFavor(fromId,toId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status",status);
        return ResultUtils.getResult(jsonObject,"success");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "举报人id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "被举报人id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "title",value = "标题",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "description",value = "描述",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "complainPhoto1",value = "举报描述图片1",dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "complainPhoto2",value = "举报描述图片2",dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "complainPhoto3",value = "举报描述图片3",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "举报用户",notes = "repeatWrong：该用户举报人太多次，不给举报了（24小时内最多5次） success：成功 ")
    @PostMapping("/complain")
    public Result<JSONObject> complainUser(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId,
                                           @RequestParam("title") String title,@RequestParam("description") String description,
                                           @RequestParam(value = "complainPhoto1",required = false) String complainPhoto1,
                                           @RequestParam(value = "complainPhoto2",required = false) String complainPhoto2,
                                           @RequestParam(value = "complainPhoto3",required = false) String complainPhoto3){
        log.info("正在举报用户，fromId：" + fromId + " toId：" + toId + " title：" + title + " description：" + description);
        return ResultUtils.getResult(new JSONObject(),userService.complainUser(fromId,toId,title,description,complainPhoto1,complainPhoto2,complainPhoto3));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "photo",value = "上传的图片",required = true,dataType = "file",paramType = "body")
    })
    @ApiOperation(value = "上传举报图片",notes = "fileWrong：文件为空 typeWrong：上传格式错误 success：成功 （返回json url（图片路径））")
    @PostMapping("/complainPhoto")
    public Result<JSONObject> complainPhotoUpload(@RequestParam("photo") MultipartFile file,@RequestParam("id") Long id){
        log.info("用户正在上传举报图片，id：" + id);
        String url = userService.uploadComplain(file,id);
        if(url.length() < 12){
            return ResultUtils.getResult(new JSONObject(),url);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url",url);
        return ResultUtils.getResult(jsonObject,"success");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "title",value = "标题",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "description",value = "具体内容",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "添加用户反馈",notes = "repeatWrong：用户24小时内反馈超过5次 success：成功")
    @PostMapping("/feedback")
    public Result<JSONObject> addFeedback(@RequestParam("id") Long id,@RequestParam("title") String title,
                                          @RequestParam("description") String description) {
        log.info("正在添加用户反馈，id：" + id + " title：" + title + " description：" + description);
        return ResultUtils.getResult(new JSONObject(), userService.addFeedback(id, title, description));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "numbers",value = "历史记录中discuss编号",required = true,allowMultiple = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "批量删除历史记录（施工完了）",notes = "success：成功")
    @DeleteMapping("/history")
    public Result<JSONObject> deleteHistory(@RequestParam("id") Long id,@RequestParam("numbers") List<Long> numbers) {
        log.info("正在批量删除历史记录，id：" + id);
        log.info(numbers.toString());
        return ResultUtils.getResult(new JSONObject(),userService.deleteHistory(id,numbers));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "被拉黑用户id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "把用户添加入黑名单",notes = "repeatWrong：已被添加进黑名单（可能是重复请求） success：成功")
    @PostMapping("/black")
    public Result<JSONObject> addBlack(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId){
        log.info("正在把用户加入黑名单，fromId：" + fromId + " toId：" + toId);
        return ResultUtils.getResult(new JSONObject(),userService.addBlack(fromId,toId));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "被取消拉黑用户id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "把用户移除黑名单",notes = "repeatWrong：用户已被移除黑名单（可能是重复请求） success：成功")
    @DeleteMapping("/black")
    public Result<JSONObject> removeBlack(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId){
        log.info("正在移除用户出黑名单，fromId：" + fromId + " toId：" + toId);
        return ResultUtils.getResult(new JSONObject(),userService.removeBlack(fromId,toId));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取用户黑名单列表",notes = "success：成功  （返回json blackList：拉黑列表 pages：总页数 counts：总数据量）")
    @GetMapping("/black")
    public Result<JSONObject> getBlackList(@RequestParam("id") Long id,@RequestParam("cnt") Long cnt,
                                           @RequestParam("page") Long page){
        log.info("正在获取用户黑名单列表，id：" + id + " cnt：" + cnt + " page：" + page);
        return ResultUtils.getResult(userService.getBlackList(id,page,cnt),"success");
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "被加好友用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "reason",value = "申请理由",required = true,dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "用户申请加好友（施工完了）",notes = "blackWrong：被对方拉黑了 repeatWrong：已经加好友了 success：成功（对方如果已申请加你为好友则会直接加成功）")
    @PostMapping("/friend")
    public Result<JSONObject> addFriend(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId,
                                        @RequestParam("reason") String reason){
        log.info("用户正在申请加好友，fromId：" + fromId + " toId：" + toId + " reason：" + reason);
        return ResultUtils.getResult(new JSONObject(),userService.addFriend(fromId,toId,reason));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "申请人id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "isPass",value = "是否通过（这里特殊一点，同意1 不同意2）",required = true,dataType = "int",paramType = "query")
    })
    @ApiOperation(value = "审核好友是否通过",notes = "repeatWrong：好友审核已被同意 existWrong：好友申请不存在 success：成功 申请成功好友数会加1 ")
    @PostMapping("/verifyFriend")
    public Result<JSONObject> verifyFriend(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId,
                                          @RequestParam("isPass") Integer isPass){
        log.info("正在审核好友是否通过，fromId：" + fromId + " toId：" + toId + " isPass：" + isPass);
        return ResultUtils.getResult(new JSONObject(),userService.verifyFriend(fromId,toId,isPass));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取申请加好友列表",notes = "success：成功  返回json postFriendList：申请加好友的列表  pages：页面总数  counts：数据总量")
    @GetMapping("/postFriendList")
    public Result<JSONObject> getPostFriendList(@RequestParam("id") Long id,@RequestParam("cnt") Long cnt,
                                                @RequestParam("page") Long page){
        log.info("正在获取申请的好友列表，id：" + id + " cnt：" + cnt + " page：" + page);
        return ResultUtils.getResult(userService.getPostFriend(id,cnt,page),"success");
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "被删除好友id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "删除好友",notes = "existWrong：好友不存在 success：成功")
    @DeleteMapping("/friend")
    public Result<JSONObject> deleteFriend(@RequestParam("fromId") Long fromId,@RequestParam("toId") Long toId){
        log.info("正在删除好友，id：" + fromId + " toId：" + toId);
        return ResultUtils.getResult(new JSONObject(),userService.deleteFriend(fromId,toId));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "type",value = "排序规则（1：按时间倒序 2：按昵称顺序）",required = true,dataType = "int",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取好友列表（施工完了）",notes = "success：成功 （返回json friendList：好友列表 pages：页面数 counts：数据总量）")
    @GetMapping("/friendList")
    public Result<JSONObject> getFriendList(@RequestParam("id") Long id,@RequestParam("type") Integer type,
                                            @RequestParam("cnt") Long cnt,@RequestParam("page") Long page){
        log.info("正在获取好友列表，id：" + id + " type：" + type + " cnt：" + cnt + " page：" + page);
        return ResultUtils.getResult(userService.getFriendList(id,type,cnt,page),"success");
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromId",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "toId",value = "被查询用户id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "判断是否为好友（施工完了）",notes = "success：成功 （返回json  status：1是 0不是）")
    @PostMapping("/judgeFriend")
    public Result<JSONObject> judgeWhetherFriend(@RequestParam("fromId") Long fromId,
                                                 @RequestParam("toId") Long toId){
        log.info("正在判断是否为好友，fromId：" + fromId + " toId：" + toId);
        return ResultUtils.getResult(userService.judgeFriend(fromId,toId),"success");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "username",value = "用户昵称（就是keyword，也是模糊查询）",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "根据用户昵称搜索用户",notes = "success：成功 （返回userList：用户列表  pages：页面数  counts：数据总量 ）")
    @GetMapping("/searchUser")
    public Result<JSONObject> searchUser(@RequestParam("username") String username,@RequestParam("cnt") Long cnt,
                                         @RequestParam("page") Long page){
        log.info("正在根据昵称搜索用户，username：" + username + " cnt：" + cnt + " page：" + page);
        return ResultUtils.getResult(userService.searchUser(username,page,cnt),"success");
    }

}