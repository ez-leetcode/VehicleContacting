package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.User;
import org.springframework.web.multipart.MultipartFile;


public interface UserService {

    String register(String phone,String code,String password);

    String judgeCode(String phone,Integer type);

    String uploadPhoto(MultipartFile file, String id);

    User getUser(Long id);

    String patchUser(Long id,String username,String sex);

    String loginByCode(String phone,String code);

    JSONObject generateToken(String phone);

    String changePassword(String phone,String code,String oldPassword,String newPassword);

    String findPassword(String phone,String code,String newPassword);
}