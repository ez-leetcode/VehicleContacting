package com.vehiclecontacting.quartz;

import com.vehiclecontacting.mapper.DiscussMapper;
import com.vehiclecontacting.pojo.Discuss;
import com.vehiclecontacting.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class RefreshScanCountsJob implements Job {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private DiscussMapper discussMapper;

    //刷新所有浏览量信息
    @Override
    public void execute(JobExecutionContext jobExecutionContext){
        log.info("正在刷新浏览量");
        //先获取浏览量
        Map<Long,Integer> map = redisUtils.getAllScan();
        Long startTime = System.nanoTime();
        for(Long x:map.keySet()){
            //原浏览量
            Discuss discuss = discussMapper.selectById(x);
            int value = map.get(x);
            if(discuss != null){
                //更新帖子浏览量
                discuss.setScanCounts(discuss.getScanCounts() + value);
                discussMapper.updateById(discuss);
            }
            //遍历删除，redis不会缓存雪崩
            redisUtils.delete("scan_" + x);
            log.info("帖子浏览量更新成功，商品：" + x + " 新增浏览量：" + value);
        }
        Long endTime = System.nanoTime();
        log.info("本次帖子访问量同步成功, 总耗时: {}", (endTime - startTime) / 1000000 + "ms");
        log.info("帖子浏览量同步成功");
    }


}
