package com.xuecheng.filesystem.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.filesystem.service.FileSystemService;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class FileSystemServiceimpl implements FileSystemService {

        @Autowired
        private FileSystemRepository fileSystemRepository;

    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    String charset;
        @Override
    public UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata) {
        //将文件上传到fastdfs  得到文件id
            String fileid = uploadDFS(multipartFile);
           if(StringUtils.isEmpty(fileid)){
               ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_BUSINESSISNULL);
           }
            //将文件的id  文件其他信息  存储到mongodb 中
            FileSystem fileSystem=new FileSystem();
            fileSystem.setFileId(fileid);
            fileSystem.setFilePath(fileid);
            fileSystem.setFiletag(filetag);
            fileSystem.setBusinesskey(businesskey);
            fileSystem.setFileName(multipartFile.getOriginalFilename());
            fileSystem.setFileType(multipartFile.getContentType());
            if(org.apache.commons.lang3.StringUtils.isNotEmpty(metadata)){
                Map map = JSON.parseObject(metadata, Map.class);
                fileSystem.setMetadata(map);
            }

            fileSystemRepository.save(fileSystem);

            return new UploadFileResult(CommonCode.SUCCESS,fileSystem);
    }

    /**
     * 上传文件 fast dfs
     * @param multipartFile  文件本身
     * @return  文件id
     */
    private String uploadDFS(MultipartFile multipartFile){
        //判断文件是否存在
        if(multipartFile==null){
            ExceptionCast.cast(FileSystemCode.FS_DELETEFILE_NOTEXISTS);
        }
        //初始化环境
        initFastDfs();
        //创建trackerClient
        TrackerClient trackerClient=new TrackerClient();

        try {
            //获取连接
            TrackerServer connection = trackerClient.getConnection();
            //获取Storerage 服务器
            StorageServer storeStorage = trackerClient.getStoreStorage(connection);
            //创建一个StoreageClient 服务器  来上传文件
            StorageClient1 storageClient1=new StorageClient1(connection,storeStorage);
            //上传文件通过字节上传
            byte[] bytes = multipartFile.getBytes();
            //得到文件的原始名称
            String filename = multipartFile.getOriginalFilename();
            String ex = filename.substring(filename.lastIndexOf(".") + 1);
            String fileid = storageClient1.upload_file1(bytes, ex, null);
            return fileid;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化 环境
     */
    private void initFastDfs(){
        //初始化tracker服务  ip是通过英文半角分割
        try {
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_charset(charset);
            //连接时间
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
            //超时时间
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
        } catch (Exception e) {
            e.printStackTrace();
            //跑出异常
            ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
        }
    }

}
