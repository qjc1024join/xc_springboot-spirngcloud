package com.xuecheng.manage_cms.dao;


import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms.config.MongoConfig;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRespository {
    
    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MongoConfig mongoConfig;
    @Autowired
    GridFSBucket gridFSBucket;
    @Test
    public void TestRes(){
        List<CmsPage> all = cmsPageRepository.findAll();
        for (CmsPage cmsPage : all) {
            System.out.println(cmsPage);
        }
    }
    @Test
    public void TestLimit(){
        Pageable pageable= PageRequest.of(0,10);
        Page<CmsPage> all = 
                cmsPageRepository.findAll(pageable);
        for (CmsPage cmsPage : all) {
            System.out.println(cmsPage);
        }
    }
    @Test
    public void findAll(){
        CmsPage cmsPage=new CmsPage();
        //查询条件
        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
        ExampleMatcher exampleMatcher=ExampleMatcher.matching();
        Pageable pageable= PageRequest.of(0,10);
        Example<CmsPage> example=Example.of(cmsPage,exampleMatcher);
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> content = all.getContent();
        System.out.println(content);
    }
    @Test
    public void setRestTemplate(){
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
        Map body = forEntity.getBody();
        System.out.println(body);
    }
    @Autowired
    GridFsTemplate gridFsTemplate;

    /**
     * 存文件
     */
    @Test
    public void setGridFsTemplate() throws FileNotFoundException {
        FileInputStream fileInputStream=new FileInputStream(new File("d:/index_banner.ftl"));
        ObjectId store = gridFsTemplate.store(fileInputStream, "index_banner.ftl");
        System.out.println(store);
    }

    /**
     * 取文件
     */
    @Test
    public void setCmsPageRepository() throws IOException {

        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("5ccbd9790d4dd11fa0a12422")));

        //打开下载流
        GridFSDownloadStream gridFSDownloadStream= gridFSBucket.openDownloadStream(gridFSFile.getObjectId());

        GridFsResource gridFsResource=new GridFsResource(gridFSFile,gridFSDownloadStream);
        String string = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
        System.out.println(string);
    }
}
