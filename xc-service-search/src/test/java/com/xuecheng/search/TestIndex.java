package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestIndex {
    /**
     * 高等级
     */
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    /**
     * 低等级
     */
    @Autowired
    private RestClient RestClient;

    /**
     * 删除索引库
     */
    @Test
    public void deleteIndex() throws IOException {
        //创建删除索引的对象
        DeleteIndexRequest deleteIndexRequest=new DeleteIndexRequest("xc_course");

        //操作索引的客户端
        IndicesClient indices = restHighLevelClient.indices();

        //执行删除索引
        DeleteIndexResponse delete = indices.delete(deleteIndexRequest);
        //拿到响应
        boolean acknowledged = delete.isAcknowledged();
        System.out.println(acknowledged);
    }
    /**
     * 创建索引库
     */
    @Test
    public void testCreateIndex() throws IOException {
        //创建索引对象
        CreateIndexRequest createIndexRequest=new CreateIndexRequest("xc_course");
        //设置参数
        createIndexRequest.settings(Settings.builder().put("number_of_shards",1).put("number_of_replicas",0));
        createIndexRequest.mapping("doc","{\n" +
                "\"properties\": {\n" +
                "\"name\": {\n" +
                "\"type\": \"text\",\n" +
                "\"analyzer\":\"ik_max_word\",\n" +
                "\"search_analyzer\":\"ik_smart\"\n" +
                "},\n" +
                "\"description\": {\n" +
                "\"type\": \"text\",\n" +
                "\"analyzer\":\"ik_max_word\",\n" +
                "\"search_analyzer\":\"ik_smart\"\n" +
                "},\n" +
                "\"studymodel\": {\n" +
                "\"type\": \"keyword\"\n" +
                "},\n" +
                "\"price\": {\n" +
                "\"type\": \"float\"\n" +
                "},\n" +
                "\"timestamp\": {\n" +
                "\"type\": \"date\",\n" +
                "\"format\": \"yyyy‐MM‐dd HH:mm:ss||yyyy‐MM‐dd||epoch_millis\"\n" +
                "}\n" +
                "}\n" +
                "}", XContentType.JSON);
        //操作索引客户端
        IndicesClient indices = restHighLevelClient.indices();
        //执行创建索引
        CreateIndexResponse indexResponse = indices.create(createIndexRequest);
        //创建成功返回响应值
        boolean responseAcknowledged = indexResponse.isAcknowledged();
        System.out.println(responseAcknowledged);
    }

    //添加文档
    @Test
    public void testAddDoc() throws IOException {
//准备json数据
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "spring cloud实战");
        jsonMap.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud 基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel", "201001");
        SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy‐MM‐dd HH:mm:ss");
        jsonMap.put("timestamp", dateFormat.format(new Date()));
        jsonMap.put("price", 5.6f);
//索引请求对象
        IndexRequest indexRequest = new IndexRequest("xc_course","doc");
//指定索引文档内容
        indexRequest.source(jsonMap);
//索引响应对象
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest);

        //获取响应结果
        DocWriteResponse.Result result = indexResponse.getResult();
        System.out.println(result);
    }

    @Test
    public void getDoc() throws IOException {
        //创建获取索引对象
        GetRequest getRequest=new GetRequest("xc_course","doc","Kaxc2moBIM8fqLJHPPgR");
        //获取响应对象
        GetResponse getResponse = restHighLevelClient.get(getRequest);
        Map<String, Object> map = getResponse.getSourceAsMap();
        for (Map.Entry<String, Object> objectEntry : map.entrySet()) {
            String name=objectEntry.getKey()+":"+objectEntry.getValue();
            System.out.println(name);
        }
    }
}
