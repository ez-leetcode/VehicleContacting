package com.vehiclecontacting.service;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20190711.SmsClient;
import com.tencentcloudapi.sms.v20190711.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;
import com.vehiclecontacting.config.TencentSmsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class SmsServiceImpl implements SmsService{

    @Override
    public boolean sendSms(String phone, String code,int templateCode) {
        log.info("正在发送短信验证码，电话：" + phone);
        log.info("验证码：" + code);
        log.info("模板：" + templateCode);
        try{
            Credential credential = new Credential(TencentSmsConfig.ACCESS_ID,TencentSmsConfig.ACCESS_KEY);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setConnTimeout(60);
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setSignMethod("HmacSHA256");
            clientProfile.setHttpProfile(httpProfile);
            //实例化sms的client对象
            SmsClient smsClient = new SmsClient(credential,"ap-guangzhou",clientProfile);
            SendSmsRequest request = new SendSmsRequest();
            request.setSmsSdkAppid("1400520404");
            request.setSign("小青龙XiaoQingL");
            request.setTemplateID(TencentSmsConfig.TEMPLATE.get(templateCode));
            //腾讯云手机号要+86表示
            String [] phoneNumber = {"+86" + phone};
            request.setPhoneNumberSet(phoneNumber);
            //模板参数，放验证码
            String [] templateParams = {code};
            request.setTemplateParamSet(templateParams);
            /* 通过 client 对象调用 SendSms 方法发起请求。注意请求方法名与请求对象是对应的
             * 返回的 res 是一个 SendSmsResponse 类的实例，与请求对象对应 */
            log.info("到这了");
            SendSmsResponse response = smsClient.SendSms(request);
            log.info(SendSmsResponse.toJsonString(response));
        }catch (Exception e){
            e.printStackTrace();
            log.error("发送短信出现错误");
            return false;
        }
        log.info("发送短信成功");
        return true;
    }

}