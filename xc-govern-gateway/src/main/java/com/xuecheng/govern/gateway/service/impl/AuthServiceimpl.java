package com.xuecheng.govern.gateway.service.impl;

import com.xuecheng.framework.utils.CookieUtil;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceimpl implements AuthService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    /**
     * 获取cookie
     * @param request
     * @return
     */
    @Override
    public String getTokenFromCookie(HttpServletRequest request) {
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String access_token = cookieMap.get("uid");
        if(StringUtils.isEmpty(access_token)){
            return null;
        }
        return access_token;
    }

    /**
     * 获取令牌
     * @param request
     * @return
     */
    @Override
    public String getJwtFromHeader(HttpServletRequest request) {
        //取出头信息 令牌
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            return null;
        }
        if(!authorization.startsWith("Bearer ")){
            return null;
        }
        //取出jwt 令牌
        String substring = authorization.substring(7);
        return substring;
    }

    /**
     * 查询数据库
     * @param tokenFromCookie
     * @return
     */
    @Override
    public long getExpire(String tokenFromCookie) {
        //key
        String key = "user_token:"+tokenFromCookie;
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire;
    }
}
