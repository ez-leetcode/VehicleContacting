package com.vehiclecontacting.config;


import com.vehiclecontacting.quartz.HotDiscussJob;
import com.vehiclecontacting.quartz.HotKeywordJob;
import com.vehiclecontacting.quartz.RefreshScanCountsJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    //浏览量刷新任务
    @Bean
    public JobDetail refreshScanCountsDetail(){
        //关联业务类
        return JobBuilder.newJob(RefreshScanCountsJob.class)
                //给JobDetail起名字
                .withIdentity("refreshScanDetail")
                .storeDurably()
                .build();
    }


    @Bean
    public Trigger refreshScanCountsTrigger(){
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0 */1 * * * ?");
        return TriggerBuilder.newTrigger()
                //关联上述JobDetail
                .forJob(refreshScanCountsDetail())
                .withIdentity("refreshScanTrigger")
                .withSchedule(cronScheduleBuilder)
                .build();
    }


    //首页热帖任务
    @Bean
    public JobDetail hotDiscussDetail(){
        return JobBuilder.newJob(HotDiscussJob.class)
                .withIdentity("hotDiscussDetail")
                .storeDurably()
                .build();
    }


    @Bean
    public Trigger hotDiscussTrigger(){
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0 */15 * * * ?");
        return TriggerBuilder.newTrigger()
                .forJob(hotDiscussDetail())
                .withIdentity("hotDiscussTrigger")
                .withSchedule(cronScheduleBuilder)
                .build();
    }


    //搜索热词任务
    @Bean
    public JobDetail hotKeywordDetail(){
        return JobBuilder.newJob(HotKeywordJob.class)
                .withIdentity("hotKeywordDetail")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger hotKeywordTrigger(){
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0 */30 * * * ?");
        return TriggerBuilder.newTrigger()
                .forJob(hotKeywordDetail())
                .withIdentity("hotKeywordTrigger")
                .withSchedule(cronScheduleBuilder)
                .build();
    }


}
