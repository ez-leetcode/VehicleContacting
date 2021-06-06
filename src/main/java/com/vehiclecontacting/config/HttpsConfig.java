package com.vehiclecontacting.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;

@Slf4j
//@Configuration
public class HttpsConfig {

    //@Bean
    public TomcatServletWebServerFactory servletContainer(){
        log.info("正在添加安全性约束");
        TomcatServletWebServerFactory tomcatServletWebServerFactory = new TomcatServletWebServerFactory(){
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint constraint = new SecurityConstraint();
                constraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                constraint.addCollection(collection);
                context.addConstraint(constraint);
            }
        };
        tomcatServletWebServerFactory.addAdditionalTomcatConnectors();
        return tomcatServletWebServerFactory;
    }


}

/*
#https加密配置
 ssl:
    #证书路径
    key-store: classpath:rat403.cn.pfx
    #证书密码
    key-store-password: N2sjS3S3
    #证书类型
    key-store-type: PKCS12
    #开启ssl
    enabled: true
 */