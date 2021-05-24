package com.vehiclecontacting.service;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.mapper.VehicleMapper;
import com.vehiclecontacting.msg.VehicleJudgeMsg;
import com.vehiclecontacting.msg.VehicleMsg;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.pojo.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AdministratorServiceImpl implements AdministratorService{

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailService mailService;



    @Override
    public String judgeVehicle(String license, Integer isPass, String reason) {
        Vehicle vehicle = vehicleMapper.selectById(license);
        if(vehicle == null){
            log.error("审核车辆失败，车辆不存在");
            return "existWrong";
        }
        if(vehicle.getIsPass() != 0){
            log.error("审核车辆失败，车辆未在审核中");
            return "repeatWrong";
        }
        //发邮件提醒，短信自定义不了文字内容
        vehicle.setIsPass(isPass);
        if(isPass != 1){
            vehicle.setBackReason(reason);
        }
        vehicle.setPassTime(new Date());
        vehicleMapper.updateById(vehicle);
        log.info("审核车辆信息成功");
        //记得同步通知
        User user = userMapper.selectById(vehicle.getId());
        if(user.getEmail() != null){
            mailService.sendJudgeEmail(user.getEmail(),license,isPass,reason);
        }
        return "success";
    }


    @Override
    public JSONObject getVehicleList(Long cnt, Long page, String keyword) {
        JSONObject jsonObject = new JSONObject();
        Page<Vehicle> page1 = new Page<>(page,cnt);
        QueryWrapper<Vehicle> wrapper = new QueryWrapper<>();
        wrapper.eq("is_pass",0)
                .orderByDesc("update_time");
        if(keyword != null && !keyword.equals("")){
            wrapper.like("license",keyword);
        }
        vehicleMapper.selectPage(page1,wrapper);
        List<Vehicle> vehicleList = page1.getRecords();
        List<VehicleJudgeMsg> vehicleMsgList = new LinkedList<>();
        for(Vehicle x:vehicleList){
            User user = userMapper.selectById(x.getId());
            VehicleJudgeMsg vehicleJudgeMsg = new VehicleJudgeMsg(x.getLicense(),x.getType(),user.getId(),user.getUsername(),user.getPhoto(),x.getVehiclePhoto1(),user.getSex(),
                    user.getVip(),x.getBackReason(),x.getUpdateTime());
            vehicleMsgList.add(vehicleJudgeMsg);
        }
        jsonObject.put("vehicleList",vehicleMsgList);
        jsonObject.put("counts",page1.getTotal());
        jsonObject.put("pages",page1.getPages());
        log.info("获取待审车辆信息成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }

}
