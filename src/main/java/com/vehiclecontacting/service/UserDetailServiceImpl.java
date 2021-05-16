package com.vehiclecontacting.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vehiclecontacting.mapper.RoleMapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.mapper.UserRoleMapper;
import com.vehiclecontacting.pojo.Role;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.pojo.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        log.info("正在尝试登录");
        //先查询用户名是否存在
        log.info("正在查询用户名是否存在：" + s);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",s);
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            log.warn("用户不存在或已被冻结");
            throw new UsernameNotFoundException("用户不存在");
        }
        QueryWrapper<UserRole> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("user",user.getId());
        Collection<GrantedAuthority> authList = new ArrayList<>();
        List<UserRole> userRoleList = userRoleMapper.selectList(wrapper1);
        for(UserRole x:userRoleList){
            //获取用户角色
            Role role = roleMapper.selectById(x.getRole());
            //把角色赋予用户
            authList.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
        }
        log.info("用户拥有的权限：" + authList.toString());
        return new org.springframework.security.core.userdetails.User(user.getId().toString(),user.getPassword(),authList);
    }
}