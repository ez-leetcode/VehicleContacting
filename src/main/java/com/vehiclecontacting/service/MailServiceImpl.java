package com.vehiclecontacting.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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

}
