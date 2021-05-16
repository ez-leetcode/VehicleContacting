package com.vehiclecontacting.service;

public interface SmsService {

    boolean sendSms(String phone,String code,int templateCode);

}
