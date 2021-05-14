package com.vehiclecontacting.demo;

import com.vehiclecontacting.mapper.RoleMapper;
import com.vehiclecontacting.pojo.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private RoleMapper roleMapper;

    @Test
    void contextLoads() {
        Role role = new Role(2,"ADMIN",null,null);
        int result = roleMapper.insert(role);
        System.out.println(result);
    }

}
