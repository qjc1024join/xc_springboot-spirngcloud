package com.xuecheng.manage_cms_client.service.impl;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import com.xuecheng.manage_cms_client.service.PageService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

@Service
public class PageServiceImpl implements PageService {


    private static final Logger  LOGGER= LoggerFactory.getLogger(PageServiceImpl.class);
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;
    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;
    /**
     * 根据页面id 查询服务器物理路径
     * @param pageId
     */
    @Override
    public void savePageToServerPath(String pageId) {
        //根据页面id 页面详情
                CmsPage cmsPage = findCmsPageById(pageId);
        //得到html 文件的id
        String htmlFileId = cmsPage.getHtmlFileId();
        //根据文件的id 查询文件的内容
        InputStream inputStream = getFileById(htmlFileId);
        if(inputStream==null){
            LOGGER.error("getFileById()");
            return;
        }
        //得到站点的信息
        CmsSite cmsSite = findSitePageById(cmsPage.getSiteId());
        //得到站点的物理路径
        String sitePhysicalPath = cmsSite.getSitePhysicalPath();
        //得到页面的物理路径
        String pagePath=sitePhysicalPath+cmsPage.getPagePhysicalPath()+cmsPage.getPageName();
        System.out.println(pagePath);
        //将HTML文件保存到服务器的物理路径
        FileOutputStream fileOutputStream=null;
        try {
             fileOutputStream=new FileOutputStream(new File(pagePath));
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 根据文件的id 查询文件的内容
     * @param htmlFileId
     * @return
     */
    public InputStream getFileById(String htmlFileId){
        //查询文件的对象
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(htmlFileId)));
        //打开一个下载流
        GridFSDownloadStream gridFSDownloadStream=gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //定义
        GridFsResource gridFsResource=new GridFsResource(gridFSFile,gridFSDownloadStream);
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 根据id 查询页面信息
     * @param pageId
     * @return
     */
    private CmsPage findCmsPageById(String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_SELECT_ERRQUERYRESULT);
        }
        return optional.get();
    }

    /**
     * 根据站点id 查询站点信息
     * @param SiteId
     * @return
     */
    private CmsSite findSitePageById(String SiteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(SiteId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_SELECT_ERRQUERYRESULT);
        }
        return optional.get();
    }
}
