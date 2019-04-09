package com.hs.learnspringbootdatasourcerout.dao;

import com.hs.learnspringbootdatasourcerout.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 指定这是一个操作数据库的mapper
 *
 */
@Mapper
public interface SysUserMapper {
    @Select("select * from user")
     List<User> selectUsers();
}
