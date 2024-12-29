package com.example.demo.mapper;

import com.example.demo.pojo.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

/*    @Select("select * from account where username = #{username}")
    Account getByUsername(String username);*/
}
