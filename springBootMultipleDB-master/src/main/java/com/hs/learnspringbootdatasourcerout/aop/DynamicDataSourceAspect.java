package com.hs.learnspringbootdatasourcerout.aop;

import com.hs.learnspringbootdatasourcerout.annontion.RoutingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class DynamicDataSourceAspect {
	
	@Before("@annotation(com.hs.learnspringbootdatasourcerout.annontion.RoutingDataSource)")
	public void beforeSwitchDS(JoinPoint point) {
		//获取当前访问的类
		Class<?> className = point.getTarget().getClass();

		//获得访问的方法名
		String methodName = point.getSignature().getName();

		//得到方法的参数类型
		Class[] argClass = ((MethodSignature)point.getSignature()).getParameterTypes();
		String dataSource = DataSourceContextHolder.DEFAULT_DATASOURCE;

		try {
			Method method = className.getMethod(methodName, argClass);
			//判断是否存在@DS注解
			if (method.isAnnotationPresent(RoutingDataSource.class)) {
				RoutingDataSource annotation = method.getAnnotation(RoutingDataSource.class);
				//取出注解的数据源
				dataSource = annotation.value();
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		//切换数据源
		DataSourceContextHolder.setDB(dataSource);
	}

	@After("@annotation(com.hs.learnspringbootdatasourcerout.annontion.RoutingDataSource)")
	public void afterSwitchDS(JoinPoint point) {
		DataSourceContextHolder.clearDB();
	}

}
