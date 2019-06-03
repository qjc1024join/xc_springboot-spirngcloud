package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 查询数据库  用户表
 */
public interface XcUserRepository extends JpaRepository<XcUser,String> {
    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户信息
     */
    XcUser findByUsername(String username);
}
