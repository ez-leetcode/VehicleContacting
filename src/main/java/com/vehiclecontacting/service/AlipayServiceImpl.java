package com.vehiclecontacting.service;

import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vehiclecontacting.config.AlipayConfig;
import com.vehiclecontacting.mapper.AlipayOrderMapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.pojo.AlipayOrder;
import com.vehiclecontacting.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AlipayServiceImpl implements AlipayService{


    @Autowired
    private AlipayOrderMapper alipayOrderMapper;

    @Autowired
    private UserMapper userMapper;


    @Override
    public void aliPay(HttpServletResponse response, HttpServletRequest request, String goodsName, Double price, Long id) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        //初始化Alipay
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl,
                AlipayConfig.app_id,
                AlipayConfig.merchant_private_key,
                "json",
                AlipayConfig.charset,
                AlipayConfig.alipay_public_key,
                AlipayConfig.sign_type);
        //先成订单
        AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();
        log.info("正在生成订单");
        AlipayOrder alipayOrder = new AlipayOrder(null,id,price,0,null,null);
        alipayOrderMapper.insert(alipayOrder);
        //获取订单编号
        QueryWrapper<AlipayOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("price",price)
                .eq("id",id)
                .orderByDesc("create_time");
        List<AlipayOrder> alipayOrderList = alipayOrderMapper.selectList(wrapper);
        AlipayOrder realOrder = alipayOrderList.get(0);
        log.info("已生成订单编号：" + realOrder.getNumber());
        //商户订单号
        String order_number = realOrder.getNumber().toString();
        //付款金额，从前台获取
        String total_amount = price.toString();
        //这里一定要转码  不然支付宝订单回调的时候回乱码，导致验签错误
        goodsName = new String(goodsName.getBytes(), StandardCharsets.UTF_8);
        alipayTradePagePayRequest.setReturnUrl(AlipayConfig.return_url);
        alipayTradePagePayRequest.setNotifyUrl(AlipayConfig.notify_url);
        alipayTradePagePayRequest.setBizContent("{\"out_trade_no\":\"" + order_number + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + goodsName + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        //请求
        String result = null;
        try {
            result = alipayClient.pageExecute(alipayTradePagePayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //输出
        PrintWriter printWriter = response.getWriter();
        printWriter.print(result);
        printWriter.flush();
        printWriter.close();
        log.info("返回结果={}",result);
    }


    @Override
    public String notifyBill(HttpServletRequest request) throws Exception{
        log.info("正在验证支付是否成功");
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            //这段代码会把正确的中文转成乱码，导致中文名称的商品不能正确反馈，坑死我了。。。
            //valueStr = new String(valueStr.getBytes("ISO_8859_1"), StandardCharsets.UTF_8);

            params.put(name, valueStr);
        }
        //是否认证成功
        boolean signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名
        if(signVerified){
            //交易成功情况下
            //商户订单号
            log.info("交易认证成功");
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            log.info("解析出的商户订单号：" + out_trade_no);
            AlipayOrder alipayOrder = alipayOrderMapper.selectById(out_trade_no);
            User user = userMapper.selectById(alipayOrder.getId());
            alipayOrder.setIsSuccess(1);
            //更新订单
            alipayOrderMapper.updateById(alipayOrder);
            //先更新vip等级测试，后面可以用了加经验条
            user.setVip(2);
            userMapper.updateById(user);
            return "success";
        }else{
            log.info("交易认证失败");
            return "fail";
        }
    }

}
