package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.User;
import org.springframework.web.multipart.MultipartFile;


public interface UserService {

    String register(String phone,String code,String password);

    String judgeCode(String phone,Integer type);

    String uploadPhoto(MultipartFile file, String id);

    User getUser(Long id,String phone);

    String patchUser(Long id,String username,String sex,String introduction);

    String loginByCode(String phone,String code);

    JSONObject generateToken(String phone);

    String changePassword(String phone,String code,String oldPassword,String newPassword);

    String findPassword(String phone,String code,String newPassword);

    String addFans(Long fromId,Long toId);

    String removeFans(Long fromId,Long toId);

    JSONObject getFans(Long id,Long cnt,Long page,String keyword);

    JSONObject getFollow(Long id,Long cnt,Long page,String keyword);

    String changeEmail(Long id,String code,String newEmail);

    String clearHistory(Long id);

    JSONObject getHistory(Long id,Long page,Long cnt);

    Integer judgeFavor(Long fromId,Long toId);

}
