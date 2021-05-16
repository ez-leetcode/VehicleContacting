package com.vehiclecontacting.service;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.mapper.UserRoleMapper;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.pojo.UserRole;
import com.vehiclecontacting.utils.JwtUtils;
import com.vehiclecontacting.utils.OssUtils;
import com.vehiclecontacting.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;


@Service
@Slf4j
public class UserServiceImpl implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public String register(String phone, String code,String password) {
        String redisCode = redisUtils.getValue("1_" + phone);
        if(redisCode == null){
            log.error("注册失败，验证码不存在或已过期");
            return "existWrong";
        }
        if(!redisCode.equals(code)){
            log.error("注册失败，验证码不正确");
            return "codeWrong";
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",phone);
        User user = userMapper.selectOne(wrapper);
        if(user != null){
            log.error("注册失败，该手机号已被人绑定");
            return "repeatWrong";
        }
        //成功注册
        User user1 = new User();
        user1.setPhone(phone);
        user1.setUsername(phone);
        //密码加密后存入
        user1.setPassword(new BCryptPasswordEncoder().encode(password));
        //创建新用户
        userMapper.insert(user1);
        //获取自动生成的id
        User user2 = userMapper.selectOne(wrapper);
        //给用户一个用户角色
        userRoleMapper.insert(new UserRole(null,user2.getId(),1,null,null));
        log.info("用户注册成功");
        log.info(user2.toString());
        //让验证码失效
        redisUtils.delete("1_" + phone);
        return "success";
    }

    @Override
    public String judgeCode(String phone, Integer type) {
        String sendCount = redisUtils.getValue("sendCode_" + phone);
        int cnt = 0;
        if(sendCount != null){
            cnt = Integer.parseInt(sendCount);
        }
        if(cnt >= 5){
            //短时间内发送短信验证码次数过多
            log.warn("用户2小时内发送短信验证码次数过多，账号已被锁定：" + phone);
            return "repeatWrong";
        }
        return "success";
    }


    @Override
    public String patchUser(Long id, String username, String sex) {
        User user = userMapper.selectById(id);
        if(user == null){
            log.error("修改用户信息失败，用户不存在：" + id);
            return "existWrong";
        }
        User user1 = new User();
        user1.setUsername(username);
        user1.setSex(sex);
        user1.setId(id);
        int result = userMapper.updateById(user1);
        if(result == 0){
            log.warn("修改用户信息出现问题，可能是重复请求：" + id);
            return "repeatWrong";
        }
        log.info("修改用户信息成功");
        return "success";
    }

    @Override
    public User getUser(Long id) {
        log.info("正在获取用户信息：" + id);
        User user = userMapper.selectById(id);
        if(user == null){
            log.warn("获取用户信息失败，用户不存在");
            return null;
        }
        log.info("获取用户信息成功，用户信息：" + user.toString());
        return user;
    }

    @Override
    public String loginByCode(String phone, String code) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",phone);
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            log.error("登录失败，用户不存在");
            return "existWrong";
        }
        if(user.getFrozenDate() != null && user.getFrozenDate().after(new Date())){
            log.error("登录失败用户已被封禁");
            return user.getFrozenDate().toString();
        }
        String redisCode = redisUtils.getValue("4_" + phone);
        log.info("正在校验验证码");
        log.info("code：" + code);
        log.info("redisCode" + redisCode);
        if(redisCode == null){
            log.error("登录失败，验证码不存在或已失效");
            return "codeExistWrong";
        }
        if(!redisCode.equals(code.toLowerCase())){
            log.error("登录失败，验证码错误");
            return "codeWrong";
        }
        log.info("短信登录验证成功");
        redisUtils.delete("4_" + phone);
        return "success";
    }


    @Override
    public JSONObject generateToken(String phone) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",phone);
        User user = userMapper.selectOne(wrapper);
        String token = JwtUtils.createToken(user.getId().toString(),user.getPassword());
        log.info("已生成token：" + token);
        log.info("用户：" + user.getUsername());
        //token生成后存入redis
        redisUtils.saveByHoursTime(user.getId().toString(),token,999);
        jsonObject.put("token",token);
        return jsonObject;
    }


    @Override
    public String changePassword(String phone, String code, String oldPassword, String newPassword) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",phone);
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            log.error("修改密码失败，用户不存在：" + phone);
            return "existWrong";
        }
        boolean judge = new BCryptPasswordEncoder().matches(oldPassword,user.getPassword());
        if(!judge){
            //旧密码错误
            log.error("修改密码失败，用户旧密码错误");
            return "oldPasswordWrong";
        }
        //从数据库取密码
        String redisCode = redisUtils.getValue("2_" + phone);
        log.info("code：" + code);
        log.info("redisCode：" + redisCode);
        if(redisCode == null){
            log.error("修改密码失败，验证码不存在");
            return "codeExistWrong";
        }
        if(!redisCode.equals(code.toLowerCase())){
            log.error("修改密码失败，验证码错误");
            return "codeWrong";
        }
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userMapper.updateById(user);
        log.info("修改密码成功");
        redisUtils.delete("2_" + phone);
        return "success";
    }

    @Override
    public String findPassword(String phone, String code, String newPassword) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",phone);
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            log.error("找回密码失败，用户不存在：" + phone);
            return "existWrong";
        }
        String redisCode = redisUtils.getValue("3_" + phone);
        log.info("code：" + code);
        log.info("redisCode：" + redisCode);
        if(redisCode == null){
            log.error("找回密码失败，验证码不存在");
            return "codeExistWrong";
        }
        if(!redisCode.equals(code.toLowerCase())){
            log.error("找回密码失败，验证码错误");
            return "codeWrong";
        }
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userMapper.updateById(user);
        log.info("找回密码成功");
        //删除验证码
        redisUtils.delete("3_" + phone);
        return "success";
    }

    @Override
    public String uploadPhoto(MultipartFile file, String id) {
        User user = userMapper.selectById(id);
        if(user == null){
            log.warn("上传头像失败，用户不存在：" + id);
            return "existWrong";
        }
        //上传阿里云oss
        String url = OssUtils.uploadPhoto(file,"userPhoto");
        if(url.length() < 12){
            //少于12说明上传报错了
            return url;
        }
        //上传成功后先删除源文件
        if(user.getPhoto() != null){
            //删除原头像文件
            log.info("正在删除原头像文件：" + user.getPhoto());
            String lastObjectName = user.getPhoto().substring(user.getPhoto().lastIndexOf("/") + 1);
            log.info("原文件名：" + lastObjectName);
            OssUtils.deletePhoto(lastObjectName,"userPhoto");
        }
        user.setPhoto(url);
        userMapper.updateById(user);
        log.info("更新头像路径成功：" + user.getPhoto());
        return user.getPhoto();
    }
}
