package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 查询公司信息
 */
public interface XcCompanyUserRepository extends JpaRepository<XcCompanyUser,String> {
    /**
     * 根据用户 id 查询 公司信息
     * @param userId 用户id
     * @return
     */
    XcCompanyUser findByUserId(String userId);
}
