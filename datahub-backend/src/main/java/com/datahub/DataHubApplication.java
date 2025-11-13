package com.datahub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;

/**
 * DataHub 数据中台启动类
 *
 * @author DataHub Team
 * @since 2025-01-01
 */
@SpringBootApplication(exclude = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
@MapperScan("com.datahub.mapper")
public class DataHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataHubApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("    DataHub 数据中台启动成功！");
        System.out.println("    接口文档: http://localhost:8080/api/doc.html");
        System.out.println("    Druid监控: http://localhost:8080/api/druid");
        System.out.println("========================================\n");
    }
}

