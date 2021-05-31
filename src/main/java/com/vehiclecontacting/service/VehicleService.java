package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.multipart.MultipartFile;

public interface VehicleService {

    String vehiclePhotoUpload(MultipartFile file,String id);

    String generateVehicle(Long id,Integer type,String license,String licensePhoto,String vehiclePhoto1,String vehiclePhoto2,String vehiclePhoto3,String description,String vehicleBrand);

    JSONObject searchVehicle(Long pages, Long cnt, String keyword,Integer type);

    JSONObject getVehicleList(Long id);

    String deleteVehicle(Long id,String license);

    String patchVehicle(Long id,String license,String licensePhoto,String description,Integer type,String vehiclePhoto1,String vehiclePhoto2,String vehiclePhoto3,String vehicleBrand);
}
