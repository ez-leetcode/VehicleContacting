package com.vehiclecontacting.config;

import java.util.HashMap;

public class TencentSmsConfig {

    public static final String ACCESS_ID = "AKIDPdbIjtmPkqfCSR9at2uVpsfBRRBepjMi";

    public static final String ACCESS_KEY = "7wUllTu4xaw1pLabNs62i6D3cuzoMcbJ";

    public static final HashMap<Integer,String> TEMPLATE = new HashMap<>();

    static {
        //注册
        TEMPLATE.put(1,"959486");
        //修改密码
        TEMPLATE.put(2,"959520");
        //找回密码
        TEMPLATE.put(3,"959521");
        //登录
        TEMPLATE.put(4,"959522");
    }

}
