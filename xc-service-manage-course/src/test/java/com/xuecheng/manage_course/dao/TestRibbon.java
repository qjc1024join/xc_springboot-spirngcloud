package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_course.client.CmsPageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRibbon {
   /*@Autowired
    private RestTemplate restTemplate;*/
    @Autowired
    private CmsPageClient cmsPageClient;
   /*@Test
    public void testRibbion(){
       // 确定要获取的名字
        String serviceName="XC-SERVICE-MANAGE-CMS";
        //从Euraka 中获取服务列表

       ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://"+serviceName+"/cms/page/get/5a754adf6abb500ad05688d9", Map.class);
       Map body = forEntity.getBody();
       System.out.println(body);

   }*/
   @Test
    public void testfeign(){
       CmsPage page = cmsPageClient.findCmsPageById("5a754adf6abb500ad05688d9");
       System.out.println(page);
   }
}
