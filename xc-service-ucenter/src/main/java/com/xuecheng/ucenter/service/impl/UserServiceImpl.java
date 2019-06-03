package com.xuecheng.ucenter.service.impl;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import com.xuecheng.ucenter.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户认证
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;

    @Autowired
    XcUserRepository xcUserRepository;

    @Autowired
    XcMenuMapper xcMenuMapper;

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    @Override
    public XcUserExt getXcUserExt(String username) {
        //根据用户名查询 xc_User的信息
        XcUser xcUser = findByUsername(username);
        //判断是否等于空
        if(xcUser==null){
            return null;
        }
        String id = xcUser.getId();
        //查询用户权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(id);
        //根据用户id 查询所属公司
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(id);
        //取出用户id
        String CompanyUserId=null;
        //判断是否为空
        if(xcCompanyUser.getCompanyId()!=null){
            CompanyUserId=xcCompanyUser.getCompanyId();
        }
        //生成对象 将数据封装进去
        XcUserExt xcUserExt=new XcUserExt();
        //将数据封装进
        BeanUtils.copyProperties(xcUser,xcUserExt);
        xcUserExt.setCompanyId(CompanyUserId);
        //设置权限
        xcUserExt.setPermissions(xcMenus);
        return xcUserExt;
    }

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    @Override
    public XcUser findByUsername(String username) {
        return xcUserRepository.findByUsername(username);
    }


}
