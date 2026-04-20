package org.xiaolong.openapisever.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.xiaolong.openapisever.exception.UnauthorizedException;
import org.xiaolong.openapisever.utils.JwtUtils; // 假设你的工具类在这里

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求 (CORS 预检请求)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 获取 Authorization 请求头
        String authHeader = request.getHeader("Authorization");

        // 2. 校验是否携带 Token 以及前缀是否为 Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("You didn't provide an API key. You need to provide your API key in an Authorization header using Bearer auth.");
        }

        // 3. 提取实际的 Token 字符串
        String token = authHeader.substring(7);

        // 4. 使用你的 JwtUtils 验证 Token (这里的具体方法名根据你的 JwtUtils 实际情况调整)
        try {
            boolean isValid = jwtUtils.validateToken(token);
            if (!isValid) {
                throw new UnauthorizedException("Invalid API key");
            }
            // 验证通过后，从 JWT subject 中解析 userId，供 Controller 记录与审计
            String userId = jwtUtils.parseToken(token);
            request.setAttribute("userId", userId);

            return true; // 验证通过，放行
        } catch (Exception e) {
            // 捕获 JwtUtils 可能抛出的过期、签名错误等异常
            throw new UnauthorizedException("Invalid API key");
        }
    }
}