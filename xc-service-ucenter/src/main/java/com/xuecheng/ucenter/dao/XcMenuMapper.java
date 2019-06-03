package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface XcMenuMapper {
    /**
     * 根据用户id 查询用的权限
     * @param userid
     * @return
     */
    public List<XcMenu> selectPermissionByUserId(String userid);
}
