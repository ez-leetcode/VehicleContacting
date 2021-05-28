package com.vehiclecontacting.quartz;

import com.vehiclecontacting.service.DiscussServiceImpl;
import com.vehiclecontacting.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HotKeywordJob implements Job {

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void execute(JobExecutionContext jobExecutionContext){
        //获取热词信息
        Map<String,Integer> map = redisUtils.getAllHotKeyword();
        for(String x:map.keySet()){
            //删除redis的数据
            redisUtils.delete("hotKeyword_" + x);
        }
        //排序
        List<Map.Entry<String,Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((Comparator.comparingInt(Map.Entry::getValue)));
        //清空热词列表
        DiscussServiceImpl.hotDiscussKeyWord.clear();
        //获取所有元素
        int ck = 0;
        for(int i = 0 ; i < 10 && i < list.size(); i++){
            ck = i;
            DiscussServiceImpl.hotDiscussKeyWord.add(list.get(list.size() - i - 1).getKey());
        }
        ck ++;
        if(ck < 10){
            //说明热词不够
            int cnt = 0;
            for(;ck < 10; ck++){
                DiscussServiceImpl.hotDiscussKeyWord.add(DiscussServiceImpl.hotDiscussKeywordTemplate.get(cnt));
                cnt ++;
            }
        }
        log.info("更新热词成功");
        log.info(DiscussServiceImpl.hotDiscussKeyWord.toString());
    }
}