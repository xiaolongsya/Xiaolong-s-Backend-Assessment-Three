package org.xiaolong.openapisever.repository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
// 确保扫描到 SecurityConfig 所在的包
@ComponentScan(basePackages = "org.xiaolong.openapisever")
public class OpenApiSeverApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenApiSeverApplication.class, args);
    }
}