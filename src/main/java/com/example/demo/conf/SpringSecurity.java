package com.example.demo.conf;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;
import java.io.IOException;

//不需要初始化器 只需要一个配置类就可开启SpringSecurity
@Configuration
//这个才可以开启功能  @EnableWebSecurity
@EnableWebSecurity
//JdbcUserDetailsManager UserDetailsService
public class SpringSecurity {
    //也可以在controller上注解 一个之前一个之后
    // @PreAuthorize("hasRole('USER')") @PostAuthorize("hasRole('USER')")
    //自定义验证
    //自己实现UserDetailsService
    //为啥自己实现 直接用啊
    //自定义登录界面 首先创建一个方法返回SecurityFilterChain 传入一个HttpSecurity对象
    //SS集成了rememberMe 14天cookie 记住我默认内存
    // 服务器重启就丢失 可以存服务器 配置新的Bean PersistentTokenRepository
    @Bean
    public DataSource dataSource() {
        PooledDataSource dataSource = new PooledDataSource();
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/test");
        dataSource.setUsername("root");
        dataSource.setPassword("123123");
        return dataSource;
    }
    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
/*        //在启动时自动在数据库建立表 存储信息 仅第一次需要
        jdbcTokenRepository.setCreateTableOnStartup(true);*/
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,PersistentTokenRepository persistentTokenRepository) throws Exception {
        return http
                //验证请求拦截 哪些地址会被拦截
                .authorizeHttpRequests(config -> {
                    //单独放行静态资源
                    config.requestMatchers("/static/**").permitAll();
                    //USER 和 ADMIN 可以访问
                    config.requestMatchers("/").hasAnyRole("USER","ADMIN");
                    //只能 ADMIN 访问
                    config.requestMatchers("/index").hasRole("ADMIN");
                    //ADMIN访问所有
                    config.anyRequest().hasRole("ADMIN");
                    //所有请求都拦截 只要验证了就ok authenticated所有都要验证后才能访问
                    config.anyRequest().authenticated();
                    // requestMatchers只拦截
                    //config.requestMatchers("/login","/register").permitAll();
                })
                //表单登录
                .formLogin(config -> {
                    config.loginPage("/login")//配置登陆页面
                            //前后端分离不需要跳转
                            //只需要处理成功或失败之后的返回
                            .successHandler(this::onAuthenticationSuccess)
                            .failureHandler(this::onAuthenticationFailure)
/*                    .defaultSuccessUrl("/index")//登录成功后跳转
                    .failureUrl("/login?error")//登录失败跳转*/
                    .permitAll();//将登录相关地址放行
                })
                //退出配置
                .logout(config -> {
                    config  .logoutUrl("/logout")//退出路径
                            .logoutSuccessHandler(this::onLogoutSuccess)
//                            .logoutSuccessUrl("/login")//退出成功跳转
                            .permitAll();

                })
                //关闭csrf
                .csrf(AbstractHttpConfigurer::disable)
                //rememberMe
                .rememberMe(config -> {
                    config.tokenValiditySeconds(14*24*60*60)//14天
                    .key("1234567890")//自定义key
                    .rememberMeParameter("remember")//自定义参数名 登陆时候提交 默认"remember" 可以改
                    .tokenRepository(persistentTokenRepository);
                })
                .sessionManagement(conf -> conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build() ;
    }
    //前后端分离需要的
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //.write(Result.success().asJsonString());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("success");
    }
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        //.write(Result.failure().asJsonString());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("fail");
    }
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //.write(Result.success().asJsonString());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("success");
    }
    //用官方的加密方式
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    //DataSource 直接在配置类中配置
/*    @Bean
    public DataSource dataSource(){
        return new PooledDataSource("xxxxxxx","xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx);
    }*/
    //JdbcUserDetailsManager
/*    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder ,
                                                 DataSource dataSource){
//        基于数据库 却别与 内存
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
//        manager.changePassword 可实现快速改密码 还有很多方法
        //仅仅作为第一个用户测试 登陆后存入数据库
        *//*        manager.createUser(User.withUsername("admin")
                .password(passwordEncoder.encode("123123"))
                .roles("USER")
                .build()) ;*//*
        return manager ;
    }*/
    //UserDetailsService
    /*@Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("123123"))
                .roles("admin")
                .build();
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("123123"))
                .roles("user")
                .build();
        //每次都不一样
        System.out.println(passwordEncoder.encode("123123"));
        System.out.println(passwordEncoder.encode("123123"));

        //基于内存的用户信息管理器作为认证中心
        return new InMemoryUserDetailsManager(admin, user);
        //withDefaultPasswordEncoder不推荐
*//*        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("123123")
                .roles("admin")
                .build();
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("123123")
                .roles("user")
                .build();*//*
    }*/

}
