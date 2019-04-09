package com.hs.learnspringbootdatasourcerout.services;

import com.hs.learnspringbootdatasourcerout.annontion.RoutingDataSource;
import com.hs.learnspringbootdatasourcerout.bean.User;
import com.hs.learnspringbootdatasourcerout.common.DataSources;
import com.hs.learnspringbootdatasourcerout.dao.SysUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SysUserService {
    @Resource
    private SysUserMapper sysUserMapper;

    // 这个注解这时是可以省略，因为默认就是访问主库
    @RoutingDataSource
    public List<User> test1() {
        return sysUserMapper.selectUsers();
    }

    @RoutingDataSource(DataSources.SLAVE_DB)
    public List<User> test2() {
        return sysUserMapper.selectUsers();
    }
}
