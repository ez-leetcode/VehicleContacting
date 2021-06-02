package com.vehiclecontacting.config;

import com.vehiclecontacting.filter.MyUsernamePasswordFilter;
import com.vehiclecontacting.handler.MyAccessDeniedHandler;
import com.vehiclecontacting.handler.MyAuthenticationFailureHandler;
import com.vehiclecontacting.handler.MyAuthenticationSuccessHandler;
import com.vehiclecontacting.handler.MyLogoutSuccessHandler;
import com.vehiclecontacting.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true,securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyAccessDeniedHandler myAccessDeniedHandler;

    @Autowired
    private MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;

    @Autowired
    private MyAuthenticationFailureHandler myAuthenticationFailureHandler;

    @Autowired
    private MyLogoutSuccessHandler myLogoutSuccessHandler;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Bean
    public PasswordEncoder getPassword(){
        //密码加密强度：5
        return new BCryptPasswordEncoder(5);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(getPassword());
    }

    //放行静态资源
    @Override
    public void configure(WebSecurity web){
        //不通过security
        //可能还要有图片
        //swagger放行这四个，不然看不见
        web.ignoring().antMatchers("/swagger-ui.html")
                .antMatchers("/webjars/**")
                .antMatchers("/v2/**")
                .antMatchers("/websocket/**")
                .antMatchers("/swagger-resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(new MyUsernamePasswordFilter(), UsernamePasswordAuthenticationFilter.class);

        http.authorizeRequests()
                .antMatchers("/**").permitAll()
                //放行swagger
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/websocket").permitAll()
                .antMatchers("/register").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/loginByCode").permitAll()
                .anyRequest().authenticated()
                .and()
                .cors()
                .and()
                .csrf()
                .disable()
                .formLogin()
                .loginProcessingUrl("/login")
                .usernameParameter("phone")
                .passwordParameter("password")
                .successHandler(myAuthenticationSuccessHandler)
                .failureHandler(myAuthenticationFailureHandler)
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(myLogoutSuccessHandler)
                .and()
                .exceptionHandling()
                .accessDeniedHandler(myAccessDeniedHandler);
    }
}
