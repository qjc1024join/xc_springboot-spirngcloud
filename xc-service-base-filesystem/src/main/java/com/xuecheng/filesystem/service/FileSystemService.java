package com.xuecheng.filesystem.service;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileSystemService {
    /**
     * 文件上传service  接口
     * @param multipartFile   文件
     * @param filetag 文件类型
     * @param businesskey 文件标签
     * @param metadata  文件原信息
     * @return
     */
    UploadFileResult upload(MultipartFile multipartFile,String filetag,String businesskey,String metadata);
}
