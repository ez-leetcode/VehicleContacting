package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.config.RabbitmqProductConfig;
import com.vehiclecontacting.mapper.*;
import com.vehiclecontacting.msg.VehicleMsg;
import com.vehiclecontacting.msg.VehicleMsg1;
import com.vehiclecontacting.pojo.*;
import com.vehiclecontacting.utils.OssUtils;
import com.vehiclecontacting.utils.RedisUtils;
import io.swagger.models.auth.In;
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

    @Autowired
    private BlackUserMapper blackUserMapper;

    @Autowired
    private RabbitmqProductConfig rabbitmqProductConfig;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private LinkUserMapper linkUserMapper;

    @Autowired
    private ComplainVehicleMapper complainVehicleMapper;


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
    public String complainVehiclePhotoUpload(MultipartFile file, String id) {
        User user = userMapper.selectById(id);
        if(user == null){
            log.error("上传车辆图片失败，用户不存在");
            return "existWrong";
        }
        //上传阿里云oss
        return OssUtils.uploadPhoto(file,"complainVehiclePhoto");
    }

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
        if(type != 0){
            wrapper.eq("type",type);
        }
        if(keyword != null && !keyword.equals("")){
            wrapper.like("license",keyword);
        }
        wrapper.eq("is_pass",1)
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


    @Override
    public String remindUser(Long fromId, Long toId,String content) {
        User user = userMapper.selectById(fromId);
        QueryWrapper<BlackUser> wrapper = new QueryWrapper<>();
        wrapper.eq("from_id",toId)
                .eq("to_id",fromId);
        BlackUser blackUser = blackUserMapper.selectOne(wrapper);
        if(blackUser != null){
            log.error("提醒用户失败，用户已被拉入黑名单");
            return "blackWrong";
        }
        String cnt = redisUtils.getValue("remind_" + fromId);
        if(cnt != null){
            int counts = Integer.parseInt(cnt);
            if(counts >= 5){
                log.error("提醒用户失败，用户被提醒太多次");
                return "repeatWrong";
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",toId);
        jsonObject.put("title","紧急通知");
        jsonObject.put("message","用户：" + user.getUsername() + "正在焦急等待您的回复，请及时回复！" + "\n" + "相关信息：" + content);
        rabbitmqProductConfig.sendBoxMessage(jsonObject);
        //更新次数
        redisUtils.addKeyByTime("remind_" + fromId,2);
        log.info("提醒用户成功");
        return "success";
    }


    @Override
    public String remindUserConnect(Long fromId, Long toId) {
        User user = userMapper.selectById(fromId);
        User user1 = userMapper.selectById(toId);
        QueryWrapper<BlackUser> wrapper = new QueryWrapper<>();
        wrapper.eq("from_id",toId)
                .eq("to_id",fromId);
        BlackUser blackUser = blackUserMapper.selectOne(wrapper);
        if(blackUser != null){
            log.error("提醒用户亲友失败，用户已被拉入黑名单");
            return "blackWrong";
        }
        String cnt = redisUtils.getValue("remindConnector_" + fromId);
        if(cnt != null){
            int count = Integer.parseInt(cnt);
            if(count >= 5){
                log.error("提醒用户亲友失败，短期内提醒太多次");
                return "repeatWrong";
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title","一则紧急的寻友通知");
        jsonObject.put("message","用户：" + user.getUsername() + "正在急切寻找您的亲友：" + user1.getUsername() + "，请有空帮忙联系一下，谢谢！");
        QueryWrapper<LinkUser> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("id1",toId)
                .or()
                .eq("id2",toId);
        List<LinkUser> linkUserList = linkUserMapper.selectList(wrapper1);
        for(LinkUser x:linkUserList){
            if(x.getId1().equals(toId) && !x.getId2().equals(fromId)){
                jsonObject.put("id",x.getId2());
                rabbitmqProductConfig.sendBoxMessage(jsonObject);
            }else if(x.getId2().equals(toId) && !x.getId1().equals(fromId)){
                jsonObject.put("id",x.getId1());
                rabbitmqProductConfig.sendBoxMessage(jsonObject);
            }
        }
        redisUtils.addKeyByTime("remindConnector_" + fromId,2);
        log.info("提醒用户亲友成功");
        return "success";
    }

    @Override
    public String complainVehicle(Long id, String reason, String photo, String license) {
        ComplainVehicle complainVehicle = new ComplainVehicle(null,id,license,reason,photo,null,0,null);
        complainVehicleMapper.insert(complainVehicle);
        log.info("申诉车辆成功");
        return "success";
    }



}