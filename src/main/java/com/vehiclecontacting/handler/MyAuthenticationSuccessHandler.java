package com.vehiclecontacting.handler;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.utils.JwtUtils;
import com.vehiclecontacting.utils.RedisUtils;
import com.vehiclecontacting.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@Slf4j
@Component
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {


    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException {
        httpServletResponse.setHeader("Content-Type", "application/json;charset=utf-8");
        PrintWriter printWriter = httpServletResponse.getWriter();
        JSONObject jsonObject = new JSONObject();
        //注意，这里不是pojo的user
        User user = (User) authentication.getPrincipal();
        log.info("登录用户：" + user.getUsername());
        log.info(user.toString());
        com.vehiclecontacting.pojo.User user1 = userMapper.selectById(user.getUsername());
        if(user1.getIsFrozen() == 1){
            log.warn("登录失败，用户账号已被冻结，frozenDate：" + user1.getFrozenDate());
            jsonObject.put("frozenDate",user1.getFrozenDate().toString());
            printWriter.write(ResultUtils.getResult(jsonObject,"frozenWrong").toString());
            printWriter.flush();
            printWriter.close();
            return ;
        }
        //生成新的token
        String token = JwtUtils.createToken(user.getUsername(),user.getPassword());
        log.info("新生成token：" + token);
        //保存token，一小时可用
        redisUtils.saveByHoursTime(user.getUsername(),token,9999);
        jsonObject.put("token",token);
        //输出信息
        printWriter.write(ResultUtils.getResult(jsonObject, "success").toString());
        printWriter.flush();
        printWriter.close();
    }

}
