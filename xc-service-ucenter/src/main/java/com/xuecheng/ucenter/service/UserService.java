package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;

public interface UserService {
    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    public XcUserExt getXcUserExt(String username);

    /**
     * 根据用户id 查询用户信息
     * @param username
     * @return
     */
    public XcUser findByUsername(String username);
}
