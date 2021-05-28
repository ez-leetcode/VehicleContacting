package com.vehiclecontacting.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AlipayService {


    void aliPay(HttpServletResponse response, HttpServletRequest request, String goodsName, Double price, Long id) throws IOException;

    String notifyBill(HttpServletRequest request)throws Exception;

}
