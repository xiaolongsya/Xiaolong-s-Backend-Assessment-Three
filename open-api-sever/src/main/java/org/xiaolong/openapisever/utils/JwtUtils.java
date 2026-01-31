package org.xiaolong.openapisever.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils
{
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    //获取签名密钥
    public Key getSecretKey()
    {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    //生成Token
    public String generateToken(String username)
    {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSecretKey())
                .compact();
    }

    //解析Token
    public String parseToken(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    //验证Token
    public boolean validateToken(String token)
    {
        try
        {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
