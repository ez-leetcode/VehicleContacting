package com.vehiclecontacting.filter;

import com.vehiclecontacting.config.BloomFilterConfig;
import com.vehiclecontacting.utils.JwtUtils;
import com.vehiclecontacting.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
public class MyUsernamePasswordFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        //尝试获取token，没有直接放行（因为没有token会没有身份不让用接口，放行无所谓）
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
        String token = request.getHeader("token");
        if(token == null || token.equals("")){
            //没有token,直接放行，给个游客身份（不能不给身份，会跳到默认的登录界面的）
            Collection<GrantedAuthority> authList = new ArrayList<>();
            authList.add(new SimpleGrantedAuthority("ROLE_USER"));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new User("1","1",authList),null,authList));
            chain.doFilter(request,response);
            return ;
        }
        log.info("正在进行身份验证");
        //有token则获取身份信息
        if(applicationContext != null){
            RedisUtils redisUtils = (RedisUtils) applicationContext.getBean("redisUtils");
            BloomFilterConfig bloomFilterConfig = (BloomFilterConfig) applicationContext.getBean("bloomFilterConfig");
            String id = request.getHeader("id");
            if(id != null){
                //从布隆过滤器
                boolean judgeExist = redisUtils.includeByBloomFilter(bloomFilterConfig,"xql",id);
                if(!judgeExist){
                    //id不存在
                    //没有token,直接放行，给个游客身份（不能不给身份，会跳到默认的登录界面的）
                    Collection<GrantedAuthority> authList = new ArrayList<>();
                    authList.add(new SimpleGrantedAuthority("ROLE_HAC"));
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new User("1","1",authList),null,authList));
                    chain.doFilter(request,response);
                    return ;
                }
            }
            //先查询redis数据库中是否有这个token
            //先获取token中的用户名
            String username = JwtUtils.getUsername(token);
            //再获取redis中对应的token
            String redisToken = redisUtils.getValue(username);
            log.info("username：" + username);
            log.info("token：" + token);
            log.info("redisToken：" + redisToken);
            if(username != null && token.equals(redisToken)){
                //身份验证token正确，赋予角色
                log.info("正在赋予身份信息");
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = redisUtils.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                //判断token有效时间是否小于300分钟，是则重置token有效时间至24小时
                if(!redisUtils.isAfterDate(username,300)){
                    //重置token有效时间
                    log.info("token时间不足，正在重置token有效时间，token：" + token);
                    redisUtils.resetExpire(username,token,24 * 60);
                    log.info("token时间已被重置成功");
                }
            }else{
                log.info("身份信息校验错误");
                //用假的token登录，给他一个黑客身份，黑名单处理
                Collection<GrantedAuthority> authList1 = new ArrayList<>();
                authList1.add(new SimpleGrantedAuthority("ROLE_HACK"));
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new User("1","1",authList1),null,authList1));
                chain.doFilter(request,response);
                return ;
            }
        }
        log.info("身份信息：" + SecurityContextHolder.getContext().toString());
        chain.doFilter(request,response);
    }
}
