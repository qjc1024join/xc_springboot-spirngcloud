package com.xuecheng.manage_media.controller;

import com.xuecheng.api.media.MediaUploadControllerApi;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.service.MediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/media/upload")
public class MediaUploadController implements MediaUploadControllerApi {


    @Autowired
    MediaUploadService mediaUploadService;
    /**
     * 文件上传前的注册
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    @Override
    @PostMapping("/register")
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        return mediaUploadService.register(fileMd5,fileName,fileSize,mimetype,fileExt);
    }

    /**
     * 效验分块存储是否存在
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     * @return
     */
    @Override
    @PostMapping("/checkchunk")
    public CheckChunkResult checkchuk(String fileMd5, Integer chunk, Integer chunkSize) {
        return mediaUploadService.checkchuk(fileMd5,chunk,chunkSize);
    }

    /**
     * 上传文件分块
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     */
    @Override
    @PostMapping("/uploadchunk")
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        return mediaUploadService.uploadchunk(file,fileMd5,chunk);
    }

    /**
     * 文件合并
     * @param fileMd5  md5
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param mimetype  文件类型
     * @param fileExt   文件扩展名
     * @return
     */
    @Override
    @PostMapping("/mergechunks")
    public ResponseResult mergeChunk(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        return mediaUploadService.mergeChunk(fileMd5,fileName,fileSize,mimetype,fileExt);
    }
}
