## 自定义注解完成数据库切库



第一步、定义我们自己的切库注解类
自定义注解有几点需要注意：

1）@Target 是作用的目标，接口、方法、类、字段、包等等，具体看：ElementType

2）@Retention 是注解存在的范围，RUNTIME代表的是注解会在class字节码文件中存在，在运行时可以通过反射获取到，具体看：RetentionPolicy

3）允许的变量，通常都要给定默认值，比如我们使用一个service时，可以@Service，也可以@Service("xxxx")

```
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.METHOD
})
public @interface RoutingDataSource {

    String value() default DataSources.MASTER_DB;
}
```
第二步、定义需要使用的数据库及配置
1、数据库配置：application.properties，这里要注意不同db的前缀区别

```
## datasource master #
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/master?characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=466420182

## datasource slave #
spring.datasourceSlave.type=com.alibaba.druid.pool.DruidDataSource
spring.datasourceSlave.driver-class-name=com.mysql.jdbc.Driver
spring.datasourceSlave.url=jdbc:mysql://localhost:3306/slave?characterEncoding=UTF-8
spring.datasourceSlave.username=root
spring.datasourceSlave.password=466420182
```

2、定义支持的数据源id：
```
public interface DataSources {

    String MASTER_DB = "masterDB";

    String SLAVE_DB = "slaveDB";
}
```

3、定义数据库实体类并配置为多数据源的形式

这里不要忽略了通过 MapperScan 指定需要扫描的mybatis的接口类

```
@Configuration
public class DatasourceConfig {
    //destroy-method="close"的作用是当数据库连接不使用的时候,就把该连接重新放到数据池中,方便下次使用调用.
    @Bean(destroyMethod =  "close", name = DataSources.MASTER_DB)
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().type(DruidDataSource.class).build();
    }

    @Bean(destroyMethod =  "close", name = DataSources.SLAVE_DB)
    @ConfigurationProperties(prefix = "spring.datasourceSlave")
    public DataSource dataSourceSlave() {
        return DataSourceBuilder.create().type(DruidDataSource.class).build();
    }
}
```
4、配置成动态数据源：

```
@Configuration
@MapperScan(basePackages = {"com.xxx.dao"})  // 这里需要替换为实际的路径
public class MybatisConfig {

    @Autowired
    @Qualifier(Datasources.MASTER_DB)
    private DataSource masterDB;

    @Autowired
    @Qualifier(DataSources.SLAVE_DB)
    private DataSource slaveDB;

    /**
     * 动态数据源
     */
    @Bean(name = "dynamicDataSource")
    public DataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        // 默认数据源
        dynamicDataSource.setDefaultTargetDataSource(masterDB);

        // 配置多数据源
        Map<Object, Object> dsMap = Maps.newHashMap();
        dsMap.put(DataSources.MASTER_DB, masterDB);
        dsMap.put(DataSources.SLAVE_DB, slaveDB);
        dynamicDataSource.setTargetDataSources(dsMap);

        return dynamicDataSource;
    }

    @Bean
    @ConfigurationProperties(prefix = "mybatis")
    public SqlSessionFactoryBean sqlSessionFactoryBean() {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        // 配置数据源，此处配置为关键配置，如果没有将 dynamicDataSource 作为数据源则不能实现切换
        sqlSessionFactoryBean.setDataSource(dynamicDataSource());
        return sqlSessionFactoryBean;
    }
}
```
第三步、使用ThreadLocal安全的管理当前进程使用的数据源连接

```
@Slf4j
public class DataSourceContextHolder {

    /**
     * 默认数据源
     */
    public static final String DEFAULT_DATASOURCE = DataSources.MASTER_DB;

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    // 设置数据源名
    public static void setDB(String dbType) {
        log.debug("切换到{}数据源", dbType);
        contextHolder.set(dbType);
    }

    // 获取数据源名
    public static String getDB() {
        return (contextHolder.get());
    }

    // 清除数据源名
    public static void clearDB() {
        contextHolder.remove();
    }
}

```
第四步、通过编写切面，对所有我们自定义切库注解的方法进行拦截，动态的选择数据源
这里是为下一步提供铺垫，动态调整DataSourceContextHolder里存储的值，使用threadLocal来管理是为了避免多线程之间互相影响。

自定义注解，核心的处理就是写处理这个注解的逻辑，然后通过指定的拦截方案根据当前的数据做一些动态的处理。比如Spring提供的@Controller、@Service等注解，都是需要我们在配置文件里配置好需要扫描的路径，然后项目启动时，spring根据配置去指定路径读取这些配置，然后这些类才可以被spring进行管理。

这里不要忽略了默认数据源要选择主库，如果切库出现什么问题，比如配置错误等，可以保证访问主库来得到正确的结果；另外，请求完了不要忘记调用提供的clearDB的操作，防止threadLocal误用带来的内存泄露。

```
@Aspect
@Component
@Slf4j
public class DynamicDataSourceAspect {

    @Before("@annotation(RoutingDataSource)")
    public void beforeSwitchDS(JoinPoint point){

        //获得当前访问的class
        Class<?> className = point.getTarget().getClass();

        //获得访问的方法名
        String methodName = point.getSignature().getName();
        //得到方法的参数的类型
        Class[] argClass = ((MethodSignature)point.getSignature()).getParameterTypes();
        String dataSource = DataSourceContextHolder.DEFAULT_DATASOURCE;
        try {
            // 得到访问的方法对象
            Method method = className.getMethod(methodName, argClass);

            // 判断是否存在@DS注解
            if (method.isAnnotationPresent(RoutingDataSource.class)) {
                RoutingDataSource annotation = method.getAnnotation(RoutingDataSource.class);
                // 取出注解中的数据源名
                dataSource = annotation.value();
            }
        } catch (Exception e) {
            log.error("routing datasource exception, " + methodName, e);
        }
        // 切换数据源
        DataSourceContextHolder.setDB(dataSource);
    }

    @After("@annotation(RoutingDataSource)")
    public void afterSwitchDS(JoinPoint point){
        DataSourceContextHolder.clearDB();
    }
}
```
第五步、动态的取出我们在切面里设置的数据源的字符串即可
这里需要把原理介绍一下，在连接数据库时其实是先选择一个配置好的spring管理的datasource的id，就是我们之前在 DatasourceConfig 类里定义的Datasource实体类的id：masterDB 和 slaveDB。然后根据id去spring的上下文选择配置，进行数据库连接。有兴趣的可以看一下源码。

```
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        log.debug("数据源为{}", DataSourceContextHolder.getDB());
        return DataSourceContextHolder.getDB();
    }
}
```
第六步、取消自动配置数据源，使用我们这里定义的数据源配置
```
在SpringBoot启动类上通常直接使用@SpringBootApplication就可以了，这里需要调整为：

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class
})
```
使用
如何使用呢，我们简单演示一下：
```
@RestController
@RequestMapping("/test/")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    @RequestMapping("/t1")
    public List<User> getUser(){
        return sysUserService.test1();
    }

    @RequestMapping("/t2")
    public List<User> getUser2(){
        return sysUserService.test2();
    }

}
```
如此，数据库切库就OK了。如果你的系统已经有主库、从库之分了，那么赶紧在你的系统里利用起来吧。
