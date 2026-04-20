package org.xiaolong.openapisever.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF（测试阶段必须，POST 请求才不会被拦截）
                .csrf(csrf -> csrf.disable())
                // 2. 开启跨域（前端调用后端接口必备）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 3. 权限配置
                .authorizeHttpRequests(auth -> auth
                        // 可选：放行 Swagger 文档接口（如果有）
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 说明：API 鉴权由 WebMvc 拦截器（JwtAuthInterceptor）负责
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    /**
     * 跨域配置（解决前端调用后端接口的跨域问题）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许前端域名访问（* 表示允许所有，生产环境要指定具体域名）
        configuration.addAllowedOriginPattern("*");
        // 允许所有请求方法（GET/POST/PUT/DELETE 等）
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许所有请求头（包括 Authorization、Content-Type 等）
        configuration.addAllowedHeader("*");
        // 允许携带 Cookie（如果有登录态需要）
        configuration.setAllowCredentials(true);
        // 跨域配置生效的路径（所有接口）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}