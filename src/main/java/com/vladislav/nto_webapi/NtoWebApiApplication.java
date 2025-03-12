package com.vladislav.nto_webapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;

import javax.sql.DataSource;

@SpringBootApplication
public class NtoWebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NtoWebApiApplication.class, args);
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(@Autowired DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);

        return initializer;
    }
}
