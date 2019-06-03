package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;

public interface ConfigService {
    /**
     * 根据id 查询  config
     * @param id
     * @return
     */
    CmsConfig getConfigById(String id);

    /**
     * 根据页面id 获取一个String
     */
    String getPageHtml(String pageId);
}
