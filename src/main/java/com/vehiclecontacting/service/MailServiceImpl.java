package com.vehiclecontacting.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Service
public class MailServiceImpl implements MailService{

    @Resource
    private JavaMailSender javaMailSender;

    private static final String sender = "1006021669@qq.com";

    @Override
    public void sendEmail(String email, String yzm, String function) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(email);
        message.setSubject("小青龙：验证码");
        //function：业务功能
        message.setText("尊敬的用户，您好：\n"
                + "\n本次" + function + "请求的验证码为：" + yzm + "，该验证码15分钟内有效，请及时输入。（请勿泄露此验证码）\n"
                + "\n如非本人操作，请忽略该邮件。\n（这是一封自动发送的邮件，不需要回复）");
        javaMailSender.send(message);
    }


    @Override
    public void sendJudgeEmail(String email,String license,Integer isPass,String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(email);
        message.setSubject("小青龙：车辆信息审核");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss");
        String emailDate = sdf.format(new Date());
        if(isPass == 1){
            message.setText("尊敬的用户，您好：\n"
            + "您申请的牌照为：" + license + "的车辆信息申请已于" + emailDate + "审核通过，快去看看吧~（这是一封自动发送的邮件，不需要回复）");
        }else{
            message.setText("尊敬的用户，您好：\n"
            + "您申请的牌照为：" + license + "的车辆信息申请已于" + emailDate + "审核被管理员拒绝，具体原因：" + reason + "\n"
            + "请修改后再申请哦~（这是一封自动发送的邮件，不需要回复）");
        }
        javaMailSender.send(message);
    }

}
