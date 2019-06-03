package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 * 页面预览
 */
@Controller
public class CmsPagePreviewController extends BaseController {

    @Autowired
    ConfigService configService;
    /**
     * 页面预览
     * @param pageId
     */
    @RequestMapping(value="/cms/preview/{pageId}",method = RequestMethod.GET)
    public void  preview(@PathVariable("pageId") String pageId) throws IOException {
        //执行静态化
        String pageHtml = configService.getPageHtml(pageId);
        //获取输出流
        ServletOutputStream outputStream = response.getOutputStream();
        response.setHeader("Content‐Type","text/html;charset=utf‐8");
        response.setContentType("text/html;charset=UTF-8");
        //写出
        outputStream.write(pageHtml.getBytes("utf-8"));

    }
}
