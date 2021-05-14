package com.vehiclecontacting.service;

import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    String register(String phone,String code);

    String sendCode(String phone,Integer type);

    String uploadPhoto(MultipartFile file, String id);

}
