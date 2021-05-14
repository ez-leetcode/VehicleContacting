package com.vehiclecontacting.handler;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

//登录后，访问接口没有权限的时候调用
@Slf4j
@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException {
        //相应状态
        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        log.info("用户权限不足");
        //返回json格式
        httpServletResponse.setHeader("Content-Type","application/json;charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        PrintWriter printWriter = httpServletResponse.getWriter();
        printWriter.write(ResultUtils.getResult(jsonObject,"authorityWrong").toString());
        printWriter.flush();
        printWriter.close();
    }

}