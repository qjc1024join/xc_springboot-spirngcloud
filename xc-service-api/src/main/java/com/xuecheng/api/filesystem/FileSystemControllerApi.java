package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value="文件管理接口",description = "文件管理接口，提供课程的增、删、改、查")
public interface FileSystemControllerApi  {
    /**
     * 文件上传
     * @param multipartFile  文件
     * @param filetag   文件
     * @param businesskey   文件标签
     * @param metadata 文件原信息
     * @return
     */
    @ApiOperation("上传文件接口")
    public UploadFileResult upload(MultipartFile multipartFile,String filetag,String businesskey,String metadata);
}
