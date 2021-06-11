package com.vehiclecontacting.quartz;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.config.RabbitmqProductConfig;
import com.vehiclecontacting.mapper.DiscussMapper;
import com.vehiclecontacting.pojo.Discuss;
import com.vehiclecontacting.service.DiscussServiceImpl;
import com.vehiclecontacting.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class HotDiscussJob implements Job {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RabbitmqProductConfig rabbitmqProductConfig;

    @Autowired
    private DiscussMapper discussMapper;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        //数据哈希表
        Map<Long,Integer> map = new HashMap<>();
        //点赞表，权重3
        Map<Long,Integer> likeMap = redisUtils.getAllLike();
        //收藏表 权重10
        Map<Long,Integer> favorMap = redisUtils.getAllFavor();
        //热度值
        int hotData = 0;
        for(Long x:likeMap.keySet()){
            map.put(x,likeMap.get(x) * 3);
            redisUtils.delete("cntLike_" + x);
        }
        for(Long x:favorMap.keySet()){
            Integer k = map.get(x);
            if(k == null){
                k = 0;
            }
            hotData = Math.max(hotData,k);
            //记得别把key直接覆盖掉了
            map.put(x,favorMap.get(x) * 10 + k);
            redisUtils.delete("cntFavor_" + x);
        }
        List<Map.Entry<Long,Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((Comparator.comparingInt(Map.Entry::getValue)));
        for(int i = 1; i <= 3 ; i++){
            if(map.size() - i < 0){
                break;
            }
            if(i == 1){
                DiscussServiceImpl.hotDiscussNumber1 = list.get(map.size() - i).getKey();
                log.info("更新了热帖1：" + DiscussServiceImpl.hotDiscussNumber1);
            }else if(i == 2){
                DiscussServiceImpl.hotDiscussNumber2 = list.get(map.size() - i).getKey();
                log.info("更新了热帖2：" + DiscussServiceImpl.hotDiscussNumber2);
            }else{
                DiscussServiceImpl.hotDiscussNumber3 = list.get(map.size() - i).getKey();
                log.info("更新了热帖3：" + DiscussServiceImpl.hotDiscussNumber3);
            }
        }
        Discuss discuss = discussMapper.selectById(DiscussServiceImpl.hotDiscussNumber1);
        if(discuss != null){
            //有新的热帖
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id",0L);
            jsonObject.put("title","热帖更新提醒");
            jsonObject.put("message","美工小姐姐向您推荐最新热帖：" + discuss.getTitle() + " 热度值为：" + hotData);
            rabbitmqProductConfig.sendQuartzMessage(jsonObject);
        }
        log.info("更新热帖成功！");
    }

}