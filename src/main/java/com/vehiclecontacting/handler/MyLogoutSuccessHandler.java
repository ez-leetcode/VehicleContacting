package com.vehiclecontacting.handler;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.utils.RedisUtils;
import com.vehiclecontacting.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
public class MyLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException{
        httpServletResponse.setHeader("Content-Type","application/json;charset=utf-8");
        //获取用户实例
        String id = httpServletRequest.getParameter("id");
        User user = userMapper.selectById(id);
        if(user != null){
            log.info("正在注销用户：" + user.getId());
            //销毁redis中的token
            redisUtils.delete(user.getId().toString());
        }
        JSONObject jsonObject = new JSONObject();
        PrintWriter printWriter = httpServletResponse.getWriter();
        printWriter.write(ResultUtils.getResult(jsonObject,"logoutSuccess").toString());
        printWriter.flush();
        printWriter.close();
    }
}
