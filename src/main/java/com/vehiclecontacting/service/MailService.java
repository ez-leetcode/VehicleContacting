package com.vehiclecontacting.service;

public interface MailService {

    void sendEmail(String email,String yzm,String function);

    void sendJudgeEmail(String email,String license,Integer isPass,String reason);

}
