package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;


/**
 * @author qi
 */
public interface PageService {
    


    /**
     * 页面查询方法
     * @param page 页码从1开始
     * @param size 数据记录
     * @param queryPageRequest  查询条件 
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    /**
     * 新增页面
     * @param cmsPage  页面数据
     * @return 返回值
     */
    public CmsPageResult add(CmsPage cmsPage);

    /**
     * 根据页面id 查询页面信息
     * @param id
     * @return
     */
    CmsPage getById(String id);

    /**
     * 修改页面信息
     * @param cmsPage  页面信息
     * @return 返回修改成功
     */
    CmsPageResult edit(String id,CmsPage cmsPage);

    /**
     * 根据id 删除数据
     * @param id
     * @return
     */
    ResponseResult deleteById(String id);

    /**
     * 页面发布
     * @param pageId
     * @return
     */
    ResponseResult post(String pageId);

    CmsPageResult saveCmsPage(CmsPage cmsPage);

    /**
     * 一键发布页面
     * @param cmsPage
     * @return
     */
    CmsPostPageResult postPageQuike(CmsPage cmsPage);
}
