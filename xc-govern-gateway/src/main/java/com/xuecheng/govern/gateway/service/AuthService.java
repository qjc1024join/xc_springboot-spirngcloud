package com.xuecheng.govern.gateway.service;

import javax.servlet.http.HttpServletRequest;

public interface AuthService {
    /**
     * 获取cookie
     * @param request
     * @return
     */
    String getTokenFromCookie(HttpServletRequest request);

    /**
     * 获取令牌
     * @param request
     * @return
     */
    String getJwtFromHeader(HttpServletRequest request);

    /**
     * 查询redis
     * @param tokenFromCookie
     * @return
     */
    long getExpire(String tokenFromCookie);
}
