package com.vehiclecontacting.demo;


import com.vehiclecontacting.mapper.FansMapper;
import com.vehiclecontacting.utils.CommentUtils;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootTest
class DemoApplicationTests {


    @Test
    void f(){
        CommentUtils.judgeComment("小姐姐");
    }

}
