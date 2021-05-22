package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.multipart.MultipartFile;

public interface VehicleService {

    String vehiclePhotoUpload(MultipartFile file,String id);

    String generateVehicle(Long id,Integer type,String license,String licensePhoto,String vehiclePhoto1,String vehiclePhoto2,String vehiclePhoto3,String description);

    JSONObject searchVehicle(Long pages, Long cnt, String keyword,Integer type);

}
