package com.hs.learnspringbootdatasourcerout.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.hs.learnspringbootdatasourcerout.common.DataSources;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceCOnfig {

    /**
     * destroy-method="close"的作用是当数据库连接不使用的时候,就把该连接重新放到数据池中,方便下次使用调用.
     *
     * @return
     */
    @Bean( name = DataSources.MASTER_DB)
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().type(DruidDataSource.class).build();
    }

    @Bean( name = DataSources.SLAVE_DB)
    @ConfigurationProperties(prefix = "spring.datasourceslave")
    public DataSource dataSourceSlave() {
        return DataSourceBuilder.create().type(DruidDataSource.class).build();
    }

}
