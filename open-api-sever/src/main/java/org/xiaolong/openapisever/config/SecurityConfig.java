package org.xiaolong.openapisever.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 必须禁用 CSRF 才能发 POST 请求
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 现阶段为了测试，先放行所有接口
                );
        return http.build();
    }
}