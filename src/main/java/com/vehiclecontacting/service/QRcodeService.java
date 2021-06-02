package com.vehiclecontacting.service;

import javax.servlet.http.HttpServletResponse;

public interface QRcodeService {

    void getPersonQRcode(Long id, HttpServletResponse response)throws Exception;

    void getAlipayQRcode(Long id,String goodsName,Double price,HttpServletResponse response)throws Exception;
}

