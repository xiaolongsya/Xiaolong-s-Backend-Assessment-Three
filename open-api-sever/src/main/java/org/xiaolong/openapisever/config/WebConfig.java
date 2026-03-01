package org.xiaolong.openapisever.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.xiaolong.openapisever.interceptor.JwtAuthInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns("/v1/chat/completions"); // 排除 AI 接口，不做 JWT 校验
    }

}