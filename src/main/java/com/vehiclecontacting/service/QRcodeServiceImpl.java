package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.utils.QRcodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
@Service
public class QRcodeServiceImpl implements QRcodeService{

    @Autowired
    private UserMapper userMapper;

    @Override
    public void getPersonQRcode(Long id, HttpServletResponse response) throws Exception{
        User user = userMapper.selectById(id);
        if(id == null){
            log.error("获取个人二维码失败，用户不存在");
            return;
        }
        //根据用户信息
        String imgPath;
        if(user.getPhoto() == null){
            imgPath = "http://vehicle-contacting.oss-cn-shenzhen.aliyuncs.com/discussPhoto/d6c4e483-7653-441c-9514-642be92e0baf.png";
        }else{
            imgPath = user.getPhoto();
        }
        ServletOutputStream stream = response.getOutputStream();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",user.getId());
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        jsonObject.put("time",dateFormat.format(calendar.getTime()));
        BufferedImage image = QRcodeUtils.createQRcode(jsonObject.toString(),imgPath,true);
        ImageIO.write(image,"JPG",stream);
        stream.flush();
        stream.close();
        log.info("生成个人二维码成功");
    }

    @Override
    public void getAlipayQRcode(Long id, String goodsName, Double price, HttpServletResponse response) throws Exception {
        //创建url
        String URL = "https://47.115.128.193/payBill?id=" + id + "&goodsName=" + goodsName + "&price=" + price;
        ServletOutputStream stream = response.getOutputStream();
        BufferedImage image = QRcodeUtils.createQRcode(URL,null,false);
        ImageIO.write(image,"JPG",stream);
        stream.flush();
        stream.close();
        log.info("生成vip二维码成功");
    }



}
