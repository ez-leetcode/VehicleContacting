package com.vehiclecontacting.service;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.config.RabbitmqProductConfig;
import com.vehiclecontacting.mapper.ComplainVehicleMapper;
import com.vehiclecontacting.mapper.DiscussMapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.mapper.VehicleMapper;
import com.vehiclecontacting.msg.ComplainVehicleMsg;
import com.vehiclecontacting.msg.FrozenUserMsg;
import com.vehiclecontacting.msg.VehicleJudgeMsg;
import com.vehiclecontacting.pojo.ComplainVehicle;
import com.vehiclecontacting.pojo.Discuss;
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

    @Autowired
    private DiscussMapper discussMapper;

    @Autowired
    private RabbitmqProductConfig rabbitmqProductConfig;

    @Autowired
    private ComplainVehicleMapper complainVehicleMapper;


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
        //通知
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",vehicle.getId());
        jsonObject.put("title","车辆审核通知");
        if(isPass == 1){
            jsonObject.put("message","您申请的车牌：" + vehicle.getLicense() + "已被管理员审核通过，恭喜您！");
        }else{
            jsonObject.put("message","您申请的车牌：" + vehicle.getLicense() + "管理员审核未通过，原因：" + reason);
        }
        rabbitmqProductConfig.sendBoxMessage(jsonObject);
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
            VehicleJudgeMsg vehicleJudgeMsg = new VehicleJudgeMsg(x.getLicense(),x.getType(),x.getVehicleBrand(),user.getId(),user.getUsername(),user.getPhoto(),x.getLicensePhoto(),user.getSex(),
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
        calendar.add(Calendar.MINUTE,minutes);
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
        if(user.getFrozenDate().before(new Date()) && user.getIsFrozen() == 0){
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


    @Override
    public String deleteDiscuss(Long number,String reason) {
        Discuss discuss = discussMapper.selectById(number);
        if(discuss == null){
            log.error("删除帖子失败，帖子不存在");
            return "existWrong";
        }
        //删除帖子
        discussMapper.deleteById(number);
        //更新用户信息
        User user = userMapper.selectById(discuss.getFromId());
        user.setDiscussCounts(user.getDiscussCounts() - 1);
        userMapper.updateById(user);
        //通知用户原因待完成
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",discuss.getFromId());
        jsonObject.put("title","帖子删除通知");
        jsonObject.put("message","您的帖子由于违反社区规定，已被管理员删除，具体原因：" + reason);
        rabbitmqProductConfig.sendBoxMessage(jsonObject);
        log.info("管理员删帖成功");
        return "success";
    }


    @Override
    public String frozeSpeak(Long id, Integer hours) {
        User user = userMapper.selectById(id);
        Calendar calendar = Calendar.getInstance();
        Date date;
        calendar.add(Calendar.HOUR,hours);
        date = calendar.getTime();
        user.setNoSpeakDate(date);
        userMapper.updateById(user);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",id);
        jsonObject.put("title","禁言通知");
        jsonObject.put("message","您的发言违法社区规定，已被管理员禁言" + hours + "小时！");
        rabbitmqProductConfig.sendBoxMessage(jsonObject);
        log.info("管理员禁言成功");
        return "success";
    }

    @Override
    public JSONObject getFrozenUser(Long cnt, Long page) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("is_frozen",1)
                .orderByDesc("reopen_date");
        Page<User> page1 = new Page<>(page,cnt);
        userMapper.selectPage(page1,wrapper);
        List<User> userList = page1.getRecords();
        List<FrozenUserMsg> frozenUserMsgList = new LinkedList<>();
        for(User x:userList){
            frozenUserMsgList.add(new FrozenUserMsg(x.getId(),x.getUsername(),x.getPhoto(),x.getVip(),x.getPhone(),x.getFrozenDate(),x.getReopenDate()));
        }
        jsonObject.put("frozenUserList",frozenUserMsgList);
        jsonObject.put("counts",page1.getTotal());
        jsonObject.put("pages",page1.getPages());
        log.info("获取封禁用户列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public JSONObject getComplainVehicleList(Long id, Long cnt, Long page, Integer isPass, String keyword) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<ComplainVehicle> wrapper = new QueryWrapper<>();
        Page<ComplainVehicle> page1 = new Page<>(page,cnt);
        if(keyword != null && !keyword.equals("")){
            wrapper.like("license",keyword);
        }
        if(isPass != 3){
            wrapper.eq("is_pass",isPass);
        }
        wrapper.orderByDesc("create_time");
        complainVehicleMapper.selectPage(page1,wrapper);
        List<ComplainVehicle> complainVehicleList = page1.getRecords();
        List<ComplainVehicleMsg> complainVehicleMsgList = new LinkedList<>();
        for(ComplainVehicle x:complainVehicleList){
            User user = userMapper.selectById(x.getId());
            complainVehicleMsgList.add(new ComplainVehicleMsg(x.getNumber(),user.getId(),user.getUsername(),user.getPhoto(),user.getVip(),x.getLicense(),x.getReason(),
                    x.getIsPass(),x.getBackReason(),x.getCreateTime()));
        }
        jsonObject.put("complainVehicleList",complainVehicleMsgList);
        jsonObject.put("counts",page1.getTotal());
        jsonObject.put("pages",page1.getPages());
        log.info("获取申诉车辆列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public String judgeComplainVehicle(Long id, Long number, Integer isPass, String backReason) {
        ComplainVehicle complainVehicle = complainVehicleMapper.selectById(number);
        if(complainVehicle == null){
            log.error("审核车辆申诉失败，申诉不存在");
            return "existWrong";
        }
        if(complainVehicle.getIsPass() != 0){
            log.error("审核车辆申诉失败，申诉已被审核");
            return "repeatWrong";
        }
        complainVehicle.setIsPass(isPass);
        if(backReason != null && !backReason.equals("")){
            complainVehicle.setBackReason(backReason);
        }
        //更新状态
        complainVehicleMapper.updateById(complainVehicle);
        QueryWrapper<Vehicle> wrapper = new QueryWrapper<>();
        wrapper.eq("license",complainVehicle.getLicense())
                //通过
                .eq("is_pass",1);
        Vehicle vehicle = vehicleMapper.selectOne(wrapper);
        if(vehicle != null){
            //设置不通过
            vehicle.setIsPass(2);
            vehicleMapper.updateById(vehicle);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id",vehicle.getId());
            jsonObject.put("message","您的车牌：" + vehicle.getLicense() + "被他人申诉通过，已被下架，如有疑惑请向管理员申诉！");
            jsonObject.put("title","车牌申诉通知");
            rabbitmqProductConfig.sendBoxMessage(jsonObject);
        }
        if(isPass == 1){
            //通知审核用户
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id",complainVehicle.getId());
            jsonObject.put("message","您的申诉的车牌：" + complainVehicle.getLicense() + "已被管理员审核通过并下架了他人的车牌，请及时申请！");
            jsonObject.put("title","车牌申诉通知");
            rabbitmqProductConfig.sendBoxMessage(jsonObject);
        }else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id",complainVehicle.getId());
            jsonObject.put("message","您的申诉的车牌：" + complainVehicle.getLicense() + "管理员审核未通过，原因：" + backReason);
            jsonObject.put("title","车牌申诉通知");
            rabbitmqProductConfig.sendBoxMessage(jsonObject);
        }
        log.info("审核车辆申诉成功");
        return "success";
    }


}
