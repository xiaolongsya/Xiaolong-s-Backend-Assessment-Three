package org.xiaolong.openapisever;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.xiaolong.openapisever.mapper")
public class OpenApiSeverApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(OpenApiSeverApplication.class, args);
    }
}
