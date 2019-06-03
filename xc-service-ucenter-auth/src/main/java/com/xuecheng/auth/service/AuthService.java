package com.xuecheng.auth.service;

import com.xuecheng.framework.domain.ucenter.ext.AuthToken;

public interface AuthService {
    /**
     * 用户认证申请令牌
     * @param username 用户名
     * @param password 用户密码
     * @param clientId 客户端id
     * @param clientSecret 客户端密码
     * @return  返回令牌
     */
    AuthToken login(String username, String password, String clientId, String clientSecret);

    /**
     * 根据用户认证信息 从redis 中查询数据
     * @param cookieToken 用户认证唯一id
     * @return jwt  令牌中的信息
     */
    AuthToken getUserToken(String cookieToken);

    /**
     * 删除redis 中的tonken
     * @param cookieId  用户认证的唯一id
     * @return 是否删除成功
     */
    boolean delToken(String cookieId);

}
