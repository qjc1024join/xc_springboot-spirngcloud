package com.xuecheng.manage_cms.service.impl;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import com.xuecheng.manage_cms.service.ConfigService;
import com.xuecheng.manage_cms.service.PageService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public class ConfigServiceImpl implements ConfigService {
    @Autowired
    CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    CmsConfigRepository cmsConfigRepository;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    PageService pageService;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;
    /**
     * 根据id 查询config
     * @param id
     * @return
     */
    @Override
    public CmsConfig getConfigById(String id) {
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if(!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_SELECT_ERRQUERYRESULT);
        }
        CmsConfig cmsConfig = optional.get();
        return cmsConfig;
    }

    /**
     * 1、填写页面DataUrl
     * 在编辑cms页面信息界面填写DataUrl，将此字段保存到cms_page集合中。
     * 2、静态化程序获取页面的DataUrl
     * 3、静态化程序远程请求DataUrl获取数据模型。
     * 4、静态化程序获取页面的模板信息
     * 5、执行页面静态化
     * @param pageId
     * @return
     */
    @Override
    public String getPageHtml(String pageId) {
        //获取数据模型
        Map model = getModelById(pageId);
        if(model==null){
            //数据模型获取不到
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取页面模板

        try {
            String templeate = getTempleateById(pageId);
            if(StringUtils.isEmpty(templeate)){
                ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
            }
            //执行静态化
            String html = getStatic(templeate, model);
            return html;
        } catch (IOException e) {
            e.printStackTrace();
        }
       return null;
    }

    /**
     * 将模型 与模板 添加到
     * @param template 模板
     * @param model 模型数据
     * @return
     */
    private String getStatic(String template,Map model){
        //创建配置对象
        Configuration configuration=new Configuration(Configuration.getVersion());
        //创建模板加载器
        StringTemplateLoader stringTemplateLoader=new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",template);
        //向configuration 中配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //获取模板
        try {
            Template configurationTemplate = configuration.getTemplate("template");
            //将模板与模型数据放进去执行静态化
            String template1 = FreeMarkerTemplateUtils.processTemplateIntoString(configurationTemplate, model);
            return template1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获取模板信息  根据页面id' 获取模板信息
     * @param pageId
     * @return
     */
    private String getTempleateById(String pageId) throws IOException {
        //取出页面信息
        CmsPage cmsPage = pageService.getById(pageId);
        if(cmsPage==null){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_ISNOTEXTENTS);
        }
        String templateId = cmsPage.getTemplateId();
        if(StringUtils.isEmpty(templateId)){
            //模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //根据模板id 查询数据
        Optional<CmsTemplate> cmsTemplate =
                cmsTemplateRepository.findById(templateId);
        //判断是否拿到数据
        if(cmsTemplate.isPresent()){
            CmsTemplate template = cmsTemplate.get();
            //获取模板文件id
            String templateFileId = template.getTemplateFileId();
            //从Gried模板文件中取出数据
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));

            ObjectId objectId = gridFSFile.getObjectId();

            //打开下载流
            GridFSDownloadStream gridFSDownloadStream= gridFSBucket.openDownloadStream(objectId);

            GridFsResource gridFsResource=new GridFsResource(gridFSFile,gridFSDownloadStream);
            String string = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
            return string;
        }
        return null;
    }
    /**
     * 取出模型数据
     * @param id 通过id
     * @return
     */
    private Map getModelById(String id){
       //取出页面信息
        CmsPage cmsPage = pageService.getById(id);
        if(cmsPage==null){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_ISNOTEXTENTS);
        }
        //取出页面的dataurl
        String dataUrl = cmsPage.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过dataurl 从config中取出数据 //通过远程请求取出数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map model = forEntity.getBody();
        return model;
    }
}
