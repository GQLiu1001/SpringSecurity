package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("com.example.demo.mapper")
@SpringBootApplication
public class DemoApplication {
//locahost:8080会有账号密码的登陆界面 会一直重定向
// login logout
// _csrf要关掉

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
