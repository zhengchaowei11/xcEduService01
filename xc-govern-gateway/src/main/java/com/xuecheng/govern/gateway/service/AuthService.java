package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    //从请求头请求head
    public String getJwtFromHeader(HttpServletRequest httpServletRequest){
        String authorization = httpServletRequest.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)){
            return null;
        }
        if (!authorization.startsWith("Bearer ")){
            return null;
        }
        //取到jwt令牌
        String jwt = authorization.substring(7);
        return jwt;
    }

    //从cookie中取到令牌
    public String getTokenFromCookie(HttpServletRequest httpServletRequest){
        Map<String, String> stringMap = CookieUtil.readCookie(httpServletRequest, "uid");
        String access_token = stringMap.get("uid");
        if (StringUtils.isEmpty(access_token)){
            return null;
        }
        return access_token;
    }

    //查看令牌的有效期
    public long getExpire(String access_token){
        String key = "user_token:"+access_token;
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire;
    }
}
