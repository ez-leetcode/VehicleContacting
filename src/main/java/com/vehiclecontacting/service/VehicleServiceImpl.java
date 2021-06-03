package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.mapper.VehicleMapper;
import com.vehiclecontacting.msg.VehicleMsg;
import com.vehiclecontacting.msg.VehicleMsg1;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.pojo.Vehicle;
import com.vehiclecontacting.utils.OssUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    //修改记得isPass=0


    @Override
    public String generateVehicle(Long id, Integer type, String license, String licensePhoto, String vehiclePhoto1, String vehiclePhoto2, String vehiclePhoto3, String description, String vehicleBrand) {
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
        Vehicle vehicle1 = new Vehicle(license,id,vehicleBrand,type,licensePhoto,vehiclePhoto1,vehiclePhoto2,vehiclePhoto3,description,0,null,0,null,null,null);
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
            vehicleMsgList.add(new VehicleMsg(x.getLicense(),x.getType(),user.getId(),x.getVehicleBrand(),user.getUsername(),user.getPhoto(),user.getSex(),user.getVip(),x.getPassTime()));
        }
        jsonObject.put("vehicleList",vehicleMsgList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("搜索车辆信息成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public JSONObject getVehicleList(Long id) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<Vehicle> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id)
                .orderByDesc("create_time");
        List<Vehicle> vehicleList = vehicleMapper.selectList(wrapper);
        List<VehicleMsg1> vehicleMsg1List = new LinkedList<>();
        for(Vehicle x:vehicleList){
            vehicleMsg1List.add(new VehicleMsg1(x.getLicense(),x.getId(),x.getType(),x.getVehicleBrand(),x.getLicensePhoto(),x.getDescription(),x.getIsPass()));
        }
        jsonObject.put("vehicleList",vehicleMsg1List);
        log.info("获取用户车辆列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public String deleteVehicle(Long id, String license) {
        QueryWrapper<Vehicle> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id)
                .eq("license",license);
        Vehicle vehicle = vehicleMapper.selectOne(wrapper);
        if(vehicle == null){
            log.error("移除车辆绑定失败，车辆未被绑定");
            return "existWrong";
        }
        //删除牌照
        vehicleMapper.deleteById(license);
        //获取用户车辆数
        User user = userMapper.selectById(id);
        user.setLicenseCounts(user.getLicenseCounts() - 1);
        userMapper.updateById(user);
        log.info("移除车辆信息成功");
        return "success";
    }

    @Override
    public String patchVehicle(Long id, String license, String licensePhoto, String description, Integer type, String vehiclePhoto1, String vehiclePhoto2, String vehiclePhoto3, String vehicleBrand) {
        QueryWrapper<Vehicle> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id)
                .eq("license",license);
        Vehicle vehicle = vehicleMapper.selectOne(wrapper);
        if(vehicle == null){
            log.error("修改车辆信息失败，车辆存在或不匹配");
            return "existWrong";
        }
        Vehicle vehicle1 = new Vehicle(license,id,vehicleBrand,type,licensePhoto,vehiclePhoto1,vehiclePhoto2,vehiclePhoto3,description,null,null,null,null,null,null);
        int result = vehicleMapper.updateById(vehicle1);
        log.info("修改车辆信息成功");
        log.info("共修改了：" + result + "条");
        return "success";
    }

}
