package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.model.response.QueryResponseResult;


/**
 * @author qi
 */
public interface SiteService {
    /**
     * 查询站点名字
     * @return
     */
    public  QueryResponseResult findAllSite();

}
