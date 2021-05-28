package com.vehiclecontacting.config;

public class AlipayConfig {

    // 作为身份标识的应用ID
    public static String app_id = "2021000117638703";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key  = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCy6xkYCAElIiXmDDIVD7AfnLapLm/h6okotwV9MpxtIx2PB03ytzdYWYF1Rg6dlJwDVmVwplK4dX+2MWtVWLOAKapD8Bm+L2KG/kEmL9QA/71zTJ0zWUztC38YUdJbAh+FE5Olnj+gWObcjwFUnuBHWXg2BYGHk8Ur3/DUM9ZocSQ+Q63PF2F8DJ7U/ZlHyyp5eet8jSJ8AZm0hbcpR7CEjj5XIqMm0S1jvf8wKjE/JE2FfcnRFd99Fgn+tNwqR+3JWI71SAOqOWa0X8HTO8EeTa5LUyfWGbkmhVXM7Sek6hUaFJiiSyYfbgjL0kRf3mRu7c0rhi6d23H6SEWyrXspAgMBAAECggEAGsYBgE4aVcMxZqZmjC5HzcM2tkRjq6vcEIKqyJxdOhuqHbAbIvn1xzvFmpX8M+p8GifAkEXjVQUIMmxcwm2+lqwldXslbwdKOCct+pfnoqqiX/GUCI4Ra9tvjmUYFFRSASj3zC3NGaKUEWc47L/Fkge6bGMQigg71h/xut9y8zfTBabT81tvtrfttiCCIu3Iml+eua2EBjwbHwXfB8VpwWy7L2qiB8KC/CwsDbQ+hKS7EFoZZXGadYMk+x3632rUHlhNTDAq6apwKB49JaVYkMym4WuAJF3gyi6BijvtohuI0aef8g2Eu+73UycLoTa90Ric8kDqhsBM68vPKSPBsQKBgQDX28hU70ZMlMws+J6Gsof/pbNZLHUlH9YFdoEf8MwKVDdpmXSAd6dlO7jv/8Th1X+Uc/4Y2FJbH38uABsjduxga4uoiy/KjZh5G1+h5Av9O664i+asJt3SMWwmDoDb0Gl1dxHp5HYDqgrwuZIiryxGqqelfibEX8FqIiXTwj6DhQKBgQDUMLtdM+MQuQFQhlqGDgv4T/IVgLgDhuzDl1hom8vg5ofxRL78pAdiOq4MqQN+UvqWAj/6ffICrcpgGVue7QCrvygXCHRJ3/RIPdyd3LE1WswTX6pBlLZF0Sv9DpoIlfQOd/QnOBCGvNCebKoXtjhXWx3gLrHN6mQsizAFKn2QVQKBgClGycQUDSlPustOr2Gqef8UvHrs2WnD8LUSuBZXgWzoNcBnq7N1LflZtj+OdSOKI99LXud5ViY+m588PfRGJWU6q3YLR5RAB0wBRAQ9C13dL8fyWKXKuNgcw0VzuIhQDhp8Lly/ZyHEPltR4PPo81p//5bRzK/4b4/P/9PCbRnRAoGAGNYnaC5lmpR9EebX+67zObpp1Jh/cXI+jlpNr9jkkUuKh/QLAtlUP+wsaTDzRrsBt8NFp9VMGYIsGPl+3uhQ1smGMMP9kJQVLMuI4jVtzJf3ISFmdUyp1Rw5sOIfp3ed3H+v5VNLo+m/5XtEoYfjql3w7keo6jhQR2J3WXNyxQ0CgYEAqYWqQp25jABKBLxp5BpEgWiCoS4Om5e2LHpCTZub4quL0MyCBz1OYTFSbC+dFpzqxlx6AFXdbwRDRTA6F84rpONf88AclGlQTn6hB7pBXRms3VX4xBz0AJnEjQJVxieBcufLHx7x5cXgm0ttsfaelaPXpy6uHwq25TNE+sR5dDY=";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhc89hcc3odFy1Wb+KTmF59u1ZHuUEF4FRjwtoI1y2NHDvUgUzVKMu5FeIl47k8QbrL/a0tO8Tei/yFMVP/azMMBI3lhubIXvKk5LmuuN9v7IdWn7cnTBhPCqJEyzUPP4zhw41oDZxdmllj7oKCzkSJlU82x3yA80hj+8Xp8Nsk82q6OU2/ymwjHpS9g8m9iZx9kc4KRng21n3afNOnlWsve2vSGaSFQGND78a61e+W7lIpBUqzhK0khzuev+GSJB8atVqvyk+K0XsqYGDeHumIR7GuLTG0HCcpHUZ9WBHaicaXkPusUJdt3S6+vTYBRGyhVYtIBWUOqUWKfV0o34hQIDAQAB";

    // 服务器异步通知页面路径（支付成功或者失败会告诉这个路径）  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "https://47.115.128.193/notifyBill";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String return_url = "http://www.baidu.com";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

}
