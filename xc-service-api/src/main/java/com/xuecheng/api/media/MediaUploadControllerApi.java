package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "媒资管理接口",description = "媒资管理接口，提供文件上传，文件处理等接口")
public interface MediaUploadControllerApi {


    @ApiOperation("文件上传注册")
    public ResponseResult register(String fileMd5,
                                   String fileName,
                                   Long fileSize,
                                   String mimetype,
                                   String fileExt);

    @ApiOperation("校验文件是否存在")
    public CheckChunkResult checkchuk(String fileMd5,Integer chunk,Integer chunkSize);

    @ApiOperation("文件分块上传")
    public ResponseResult uploadchunk(MultipartFile file,String fileMd5,Integer chunk);

    @ApiOperation("分块合并")
    public ResponseResult mergeChunk(String fileMd5,
                                String fileName,
                                Long fileSize,
                                String mimetype,
                                String fileExt);
}
