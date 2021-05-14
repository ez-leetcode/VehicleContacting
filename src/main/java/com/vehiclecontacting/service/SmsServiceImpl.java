package com.vehiclecontacting.service;


import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmsServiceImpl implements SmsService{

    public static String accessKeyId = "LTAI5tAEUYp3P4DMn85ekabB";

    private static final String accessKeySecret = "luRzwpshRD1AHqEldvXe4XSKhu8Fmm";

    @Override
    public boolean sendSms(String phone, String code, String templateCode) {
        log.info("正在发送短信验证码，电话：" + phone);
        log.info("验证码：" + code);
        log.info("模板：" + templateCode);
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",accessKeyId,accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", "签名名称");
        request.putQueryParameter("TemplateCode", "模板code");
        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info(response.getData());
            return true;
        }catch (ServerException e) {
            log.error("服务器错误");
            e.printStackTrace();
        }catch (ClientException e) {
            log.error("客户端错误");
            e.printStackTrace();
        }
        return false;
    }

}
