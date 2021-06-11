package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.mapper.BoxMessageMapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.pojo.BoxMessage;
import com.vehiclecontacting.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BoxServiceImpl implements BoxService{

    @Autowired
    private BoxMessageMapper boxMessageMapper;

    @Autowired
    private UserMapper userMapper;

    //加个消息盒子数

    @Override
    public JSONObject getAllBox(Long id, Long cnt, Long page, String keyword) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<BoxMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id)
                .orderByDesc("create_time");
        if(keyword != null && !keyword.equals("")){
            wrapper.like("message",keyword);
        }
        Page<BoxMessage> page1 = new Page<>(page,cnt);
        boxMessageMapper.selectPage(page1,wrapper);
        List<BoxMessage> boxMessageList = page1.getRecords();
        //已读待更新
        jsonObject.put("messageList",boxMessageList);
        jsonObject.put("counts",page1.getTotal());
        jsonObject.put("pages",page1.getPages());
        log.info("获取消息盒子内容成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }

    @Override
    public String readAllBox(Long id) {
        QueryWrapper<BoxMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id);
        List<BoxMessage> boxMessageList = boxMessageMapper.selectList(wrapper);
        for(BoxMessage x:boxMessageList){
            x.setIsRead(1);
            boxMessageMapper.updateById(x);
        }
        //数量待完成
        User user = userMapper.selectById(id);
        user.setBoxMessageCounts(0);
        userMapper.updateById(user);
        log.info("已读所有消息成功");
        return "success";
    }


    @Override
    public String deleteBoxMessage(Long id, List<Long> numbers) {
        User user = userMapper.selectById(id);
        int cnt = 0;
        for(Long x:numbers){
            BoxMessage boxMessage = boxMessageMapper.selectById(x);
            if(boxMessage != null && boxMessage.getIsRead() == 0){
                //未读
                cnt ++;
            }
            boxMessageMapper.deleteById(x);
        }
        user.setBoxMessageCounts(user.getBoxMessageCounts() - cnt);
        userMapper.updateById(user);
        log.info("批量删除消息盒子的数据成功");
        return "success";
    }


    @Override
    public void addBoxMessage(Long id, String message, String title) {
        //待完成
        boxMessageMapper.insert(new BoxMessage(null,id,title,message,0,null));
        log.info("添加消息盒子成功");
        User user = userMapper.selectById(id);
        user.setBoxMessageCounts(user.getBoxMessageCounts() + 1);
        userMapper.updateById(user);
        log.info("更新未读条数成功");
    }

}