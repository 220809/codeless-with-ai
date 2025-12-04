package com.dingzk.codeless.generator;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class MyBatisFlexCodeGenerator {

    public static void main(String[] args) {
        final String dbConfigPrefix = "spring.datasource.";
        YAMLConfiguration config = new YAMLConfiguration();
        try {
            config.read(new ClassPathResource("application-local.yml").getInputStream());
        } catch (IOException | ConfigurationException e) {
            throw new RuntimeException(e);
        }
        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(config.get(String.class, dbConfigPrefix + "url"));
        dataSource.setUsername(config.get(String.class, dbConfigPrefix + "username"));
        dataSource.setPassword(config.get(String.class, dbConfigPrefix + "password"));

        //创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfig();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }

    public static GlobalConfig createGlobalConfig() {
        final String[] tableNames = {"user"};

        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        // 设置 Javadoc
        globalConfig.getJavadocConfig()
                .setAuthor("dingzk")
                .setSince("");

        //设置根包
        globalConfig.getPackageConfig()
                .setBasePackage("com.flex");

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(tableNames)
                // 设置逻辑删除字段
                .setLogicDeleteColumn("deleted");

        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setAlwaysGenColumnAnnotation(true)
                .setJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();

        // 设置生成 service
        globalConfig.enableService();
        globalConfig.enableServiceImpl();
        // 设置生成 controller
        globalConfig.enableController();

        return globalConfig;
    }
}