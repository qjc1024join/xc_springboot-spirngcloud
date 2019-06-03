package com.xuecheng.auth.client;

import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = XcServiceList.XC_SERVICE_UCENTER)
public interface UserClient {
    /**
     * 根据账号查询用户中心
     * @param username 用户名
     * @return 用户数据
     */
    @GetMapping("/ucenter/getuserext")
    public XcUserExt getXcUserExt(@RequestParam("username") String username);


}
