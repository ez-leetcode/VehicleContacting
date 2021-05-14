package com.vehiclecontacting.service;


import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.utils.OssUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class UserServiceImpl implements UserService{

    @Autowired
    private UserMapper userMapper;


    @Override
    public String register(String phone, String code) {
        return null;
    }

    @Override
    public String sendCode(String phone, Integer type) {
        return null;
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
