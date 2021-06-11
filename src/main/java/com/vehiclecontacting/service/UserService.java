package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface UserService {

    String register(String phone,String code,String password);

    String judgeCode(String phone,Integer type);

    String uploadPhoto(MultipartFile file, String id);

    String uploadComplain(MultipartFile file, Long id);

    User getUser(Long id,String phone);

    String patchUser(Long id,String username,String sex,String introduction,Integer isNoDisturb);

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

    String complainUser(Long fromId,Long toId,String title,String description,String complainPhoto1,String complainPhoto2,String complainPhoto3);

    String addFeedback(Long id,String title,String description);

    String addBlack(Long fromId,Long toId);

    String removeBlack(Long fromId,Long toId);

    JSONObject getBlackList(Long id,Long page,Long cnt);

    String addFriend(Long fromId,Long toId,String reason);

    String verifyFriend(Long fromId,Long toId,Integer isPass);

    JSONObject getPostFriend(Long id,Long cnt,Long page,Integer type);

    String deleteHistory(Long id, List<Long> numbers);

    String deleteFriend(Long fromId,Long toId);

    JSONObject getFriendList(Long id, Integer type,Long cnt,Long page);

    JSONObject judgeFriend(Long fromId,Long toId);

    JSONObject searchUser(String username,Long page,Long cnt);

    String linkUser(Long fromId,Long toId,String relationship);

    JSONObject getPostLinkUser(Long id,Long cnt,Long page,Integer type);

    String judgeLinkUser(Long fromId,Long toId,Integer isPass);

    JSONObject getLinkUser(Long id);

    String deleteLinkUser(Long fromId,Long toId);

    JSONObject judgeLink(Long fromId,Long toId);
}
