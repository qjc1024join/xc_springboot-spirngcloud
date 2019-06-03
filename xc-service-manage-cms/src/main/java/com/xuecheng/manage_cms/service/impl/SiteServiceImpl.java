package com.xuecheng.manage_cms.service.impl;

import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.CmsSiteRespository;
import com.xuecheng.manage_cms.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * @author qi
 */
@Service
public class SiteServiceImpl implements SiteService {

    @Autowired
    CmsSiteRespository cmsSiteRespository;

    /**
     * 查询站点名字
     * @return
     */
    @Override
    public QueryResponseResult findAllSite(){
        /**
         * 查询所有站点
         */
        List<CmsSite> all = cmsSiteRespository.findAll();
        /**
         * 将里面封装
         */
        QueryResult<CmsSite> queryResult=new QueryResult();
        /**
         * 封装到到消息里面
         */
        queryResult.setList(all);
        /**
         * 将消息封装并返回
         */
        QueryResponseResult queryResponseResult=new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return  queryResponseResult;
    }
}
