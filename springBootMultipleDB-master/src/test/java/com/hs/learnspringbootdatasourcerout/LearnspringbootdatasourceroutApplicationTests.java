package com.hs.learnspringbootdatasourcerout;

import com.hs.learnspringbootdatasourcerout.bean.User;
import com.hs.learnspringbootdatasourcerout.services.SysUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LearnspringbootdatasourceroutApplicationTests {


	@Autowired
	private SysUserService sysUserService;

	@Test
	public void contextLoads() {
		List<User> users = sysUserService.test1();
		System.out.println(users.toString());

		List<User> users1 = sysUserService.test2();

		System.out.println();
	}

}
