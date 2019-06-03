package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

public interface MediaUploadService {

    /**
     * 文件注册  检察文件是否存在
     * @param fileMd5 文件唯一标识
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param mimetype 文件类型
     * @param fileExt
     * @return
     */
    ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);

    /**
     * 检查分块存储是否存在
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     * @return
     */
    CheckChunkResult checkchuk(String fileMd5, Integer chunk, Integer chunkSize);

    /**
     * 上传文件分块
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     */
    ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk);

    /**
     * 文件合并
     * @param fileMd5 md5
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param mimetype 文件类型
     * @return
     */
    ResponseResult mergeChunk(String fileMd5, String fileName, Long fileSize, String mimetype,String fileExt);

    /**
     * 发送视屏消息  视屏消息转码
     * @param mediaId
     * @return
     */
    ResponseResult sendProcessVideoMsg(String mediaId);
}
