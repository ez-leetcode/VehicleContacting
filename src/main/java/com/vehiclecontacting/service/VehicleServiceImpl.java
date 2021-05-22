package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.mapper.VehicleMapper;
import com.vehiclecontacting.msg.VehicleMsg;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.pojo.Vehicle;
import com.vehiclecontacting.utils.OssUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class VehicleServiceImpl implements VehicleService{

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private UserMapper userMapper;



    @Override
    public String vehiclePhotoUpload(MultipartFile file, String id) {
        User user = userMapper.selectById(id);
        if(user == null){
            log.error("上传车辆图片失败，用户不存在");
            return "existWrong";
        }
        //上传阿里云oss
        return OssUtils.uploadPhoto(file,"vehiclePhoto");
    }


    @Override
    public String generateVehicle(Long id, Integer type, String license, String licensePhoto, String vehiclePhoto1, String vehiclePhoto2, String vehiclePhoto3, String description) {
        User user = userMapper.selectById(id);
        if(user == null){
            log.error("创建车辆失败，用户不存在");
            return "existWrong";
        }
        if(user.getLicenseCounts() >= 4){
            log.warn("创建车辆失败，用户上传次数过多");
            return "amountWrong";
        }
        Vehicle vehicle = vehicleMapper.selectById(license);
        if(vehicle != null){
            log.error("创建车辆失败，该车牌已被占用");
            return "repeatWrong";
        }
        //创建车辆信息
        Vehicle vehicle1 = new Vehicle(license,id,type,licensePhoto,vehiclePhoto1,vehiclePhoto2,vehiclePhoto3,description,0,0,null,null,null);
        vehicleMapper.insert(vehicle1);
        //用户车辆信息加一
        user.setLicenseCounts(user.getLicenseCounts() + 1);
        userMapper.updateById(user);
        return "success";
    }


    @Override
    public JSONObject searchVehicle(Long page, Long cnt, String keyword,Integer type) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<Vehicle> wrapper = new QueryWrapper<>();
        wrapper.eq("is_pass",1)
                .eq("type",type)
                .orderByDesc("pass_time");
        Page<Vehicle> page1 = new Page<>(page,cnt);
        vehicleMapper.selectPage(page1,wrapper);
        List<Vehicle> vehicleList = page1.getRecords();
        List<VehicleMsg> vehicleMsgList = new LinkedList<>();
        for(Vehicle x:vehicleList){
            User user = userMapper.selectById(x.getId());
            vehicleMsgList.add(new VehicleMsg(x.getLicense(),x.getType(),user.getId(),user.getUsername(),user.getPhoto(),user.getSex(),user.getVip(),x.getPassTime()));
        }
        jsonObject.put("vehicleList",vehicleMsgList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("搜索车辆信息成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


}
