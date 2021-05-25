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
import com.vehiclecontacting.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class AdministratorServiceImpl implements AdministratorService{

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private RedisUtils redisUtils;

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

    @Override
    public String frozeUser(Long id, Integer minutes) {
        User user = userMapper.selectById(id);
        if(user == null){
            log.error("冻结用户失败，用户不存在");
            return "existWrong";
        }
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.MINUTE,minutes);
        //当前时间推后minutes分钟
        user.setFrozenDate(calendar.getTime());
        user.setIsFrozen(1);
        userMapper.updateById(user);
        //发邮件通知待完成
        //删掉token，强制下线
        redisUtils.delete(id.toString());
        return "success";
    }

    @Override
    public String reopenUser(Long id) {
        User user = userMapper.selectById(id);
        if(user == null){
            log.error("解封用户失败，用户不存在");
            return "existWrong";
        }
        if(user.getFrozenDate().before(new Date())){
            log.warn("解封用户失败，用户已被解封");
            return "repeatWrong";
        }
        //先修改冻结信息
        user.setIsFrozen(0);
        user.setFrozenDate(new Date());
        userMapper.updateById(user);
        //发邮件通知待完成
        log.info("解封用户成功");
        return "success";
    }

}
