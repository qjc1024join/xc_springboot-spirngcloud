package com.xuecheng.manage_cms.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRespository;
import com.xuecheng.manage_cms.service.ConfigService;
import com.xuecheng.manage_cms.service.PageService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author qi
 */
@Service
public class PageServiceImpl implements PageService {
    @Autowired
    ConfigService configService;

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    CmsSiteRespository cmsSiteRespository;
    /**
     * 页面查询方法
     * @param page 页码从1开始
     * @param size 数据记录
     * @param queryPageRequest  查询条件
     * @return
     */
    @Override
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        //判断前端页面没有管过来条件 则自己new
        if(queryPageRequest==null){
            queryPageRequest=new QueryPageRequest();
        }
        //自定义条件的查询
        //条件匹配器
        ExampleMatcher exampleMatcher=ExampleMatcher.matching().withMatcher("pageAliase",
                ExampleMatcher.GenericPropertyMatchers.contains());
        //条件值对象
        CmsPage cmsPage=new CmsPage();
        //设置条件值
        if(StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            //站点id
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if(StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            //模板 id
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        if(StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            //模板 id
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //定义条件对象Example
        Example<CmsPage> example=Example.of(cmsPage,exampleMatcher);


        //页码
        if(page<=0){
            page=1;
        }
        page=page-1;
        if(size<=0){
            size=10;
        }
        Pageable pageable= PageRequest.of(page,size);
        //实现自定义条件查询
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);
        QueryResult queryResult=new QueryResult();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        QueryResponseResult queryResponseResult=new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    /**
     * 新增页面
     * @param cmsPage  页面数据
     * @return
     */
    @Override
    public CmsPageResult add(CmsPage cmsPage) {
      if(cmsPage==null){
          ExceptionCast.cast(CmsCode.CMS_PARAMETER_CANNOTBENULL);
      }
        //根据页面名字,站点id 页面路径的唯一性  校验  是否存在
        CmsPage pageResult = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(pageResult!=null){
            //使用全局异常捕获  获取具体异常类
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        cmsPage.setPageId(null);
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
    }

    /**
     * 根据id 查询页面信息
     * @param id
     * @return
     */
    @Override
    public CmsPage getById(String id) {
        if(id==null||id==""){
            ExceptionCast.cast(CmsCode.CMS_PARAMETER_CANNOTBENULL);
        }
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
            if(!optional.isPresent()){
              ExceptionCast.cast(CmsCode.CMS_SELECT_ERRQUERYRESULT);
            }
        CmsPage cmsPage = optional.get();
        return cmsPage;
    }

    /**
     * 根据id用数据库查询页面信息
     * @param id
     * @param cmsPage  页面信息
     * @return
     */
    @Override
    public CmsPageResult edit(String id,CmsPage cmsPage) {
        //根据id查询页面信息
        CmsPage one = this.getById(id);
        //如果查询到数据
        if(one==null){
           ExceptionCast.cast(CmsCode.CMS_SELECT_ERRQUERYRESULT);
        }
        if(cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_PARAMETER_CANNOTBENULL);
        }
        //准备更新数据
        one.setTemplateId(cmsPage.getTemplateId());
        //更新所属站点
        one.setSiteId(cmsPage.getSiteId());
        //更新页面别名
        one.setPageAliase(cmsPage.getPageAliase());
        //更新页面名称
        one.setPageName(cmsPage.getPageName());
        //更新访问路径
        one.setPageWebPath(cmsPage.getPageWebPath());
        //更新物理路径
        one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
        //添加访问地址
        one.setDataUrl(cmsPage.getDataUrl());
        //执行更新
        CmsPage save = cmsPageRepository.save(one);
        return new CmsPageResult(CommonCode.SUCCESS,save);
    }

    @Override
    public ResponseResult deleteById(String id) {
        if(StringUtils.isEmpty(id)){
            ExceptionCast.cast(CmsCode.CMS_PARAMETER_CANNOTBENULL);
        }
        //先查询一下
        Optional<CmsPage> cmsPage = cmsPageRepository.findById(id);
        //判断是否存在数据
        if(!cmsPage.isPresent()){
          ExceptionCast.cast(CmsCode.CMS_SELECT_ERRQUERYRESULT);
        }
        //存在执行删除操作
        cmsPageRepository.deleteById(id);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 页面发布
     * @param pageId
     * @return
     */
    @Override
    public ResponseResult post(String pageId) {
        //执行页面静态化
        String pageHtml = configService.getPageHtml(pageId);
        //将页面静态化存储到gridfs 中
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        //向mq 发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }



    /**
     * 向mq 发送消息去下载
     */
    private void sendPostPage(String pageId){
        //拿到页面的信息
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_SELECT_ERRQUERYRESULT);
        }
        //创建消息对象
        Map<String,String> msg=new HashMap<>();
        msg.put("pageId",pageId);
        //转成json 串
        String jsonString = JSON.toJSONString(msg);

        //得到站点id
        String siteId = cmsPage.getSiteId();

        //将消息发送给mq指定交换机  //站点id 作为routingkey
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,jsonString);
    }
    /**
     * 将html 保存到gruidFS 中
     * @param pageId
     * @param content
     * @return
     */
    private CmsPage saveHtml(String pageId,String content){
        //拿到页面的信息
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_SELECT_ERRQUERYRESULT);
        }
        //将html 的内容保存到gurid fs 中
        //将content内容转为输入流
        ObjectId objectId=null;
        try {
            InputStream inputStream = IOUtils.toInputStream(content, "utf-8");
            //根据页面名称起名字 得到文件id
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将html 的id 保存到cmspage中
        cmsPage.setHtmlFileId(objectId.toHexString());
        //修改
        CmsPage page = cmsPageRepository.save(cmsPage);
        return page;
    }

    /**
     * 添加页面有则更新  没有则添加
     * @param cmsPage
     * @return
     */
    @Override
    public CmsPageResult saveCmsPage(CmsPage cmsPage) {
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(cmsPage1!=null){
            //进行更新
           return edit(cmsPage1.getPageId(),cmsPage);
        }

        return this.add(cmsPage);
    }

    /**
     * 一键发布课程界面
     * @param cmsPage
     * @return
     */
    @Override
    public CmsPostPageResult postPageQuike(CmsPage cmsPage) {
        //将页面信息保存到数据库
        CmsPageResult pageResult = saveCmsPage(cmsPage);
        if(!pageResult.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面id
        CmsPage cmsPage1 = pageResult.getCmsPage();
        String pageId = pageResult.getCmsPage().getPageId();
        //执行发布页面向mq发送消息
        ResponseResult post = post(pageId);
        if(!post.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //发布成功  就会有一个pageurl 拼接url
        //站点id
        String siteId = cmsPage1.getSiteId();
        CmsSite siteById = findCmsSiteById(siteId);
        //拼装页面的url
        String url=siteById.getSiteDomain()+siteById.getSiteWebPath()+cmsPage1.getPageWebPath()+cmsPage1.getPageName();

        return new CmsPostPageResult(CommonCode.SUCCESS,url);
    }
    //根据id查询站点信息
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRespository.findById(siteId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }
}
