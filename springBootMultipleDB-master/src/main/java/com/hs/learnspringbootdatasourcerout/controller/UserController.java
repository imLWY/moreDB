package com.hs.learnspringbootdatasourcerout.controller;

import com.hs.learnspringbootdatasourcerout.bean.User;
import com.hs.learnspringbootdatasourcerout.services.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test/")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    @RequestMapping("/t1")
    public List<User> getUser(){
    	List<User> all = sysUserService.test1();
        return all;
    }

    @RequestMapping("/t2")
    public List<User> getUser2(){
        return sysUserService.test2();
    }

}
