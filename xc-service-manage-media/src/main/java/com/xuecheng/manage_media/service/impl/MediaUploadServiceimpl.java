package com.xuecheng.manage_media.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import com.xuecheng.manage_media.service.MediaUploadService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadServiceimpl implements MediaUploadService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String upload_location;
    @Value("${xc-service-manage-media.mq.routingkey‐media‐video}")
    String routingkey_media_video;
    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     * 文件注册  上传之前检查
     * @param fileMd5 文件唯一标识
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param mimetype 文件类型
     * @param fileExt 扩展名
     * @return
     */

    @Override
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        /*** 根据文件md5得到文件路径* 规则：* 一级目录：md5的第一个字符* 二级目录：md5的第二个字符* 三级目录：md5* 文件名：md5+文件扩展名*
         *  @param fileMd5 文件md5值* @param fileExt 文件扩展名* @return 文件路径*/
        //1.检查文件是否在本地磁盘存在
        //文件所属目录路径
        String fileFoldPath = getFileFoldPath(fileMd5);
        //文件路径
        String filePath = getFilePath(fileMd5, fileExt);
        //检查文件是否存在
        File file=new File(filePath);
        //文件是否存在
        boolean exists = file.exists();


        //2.检查文件信息是否在mongodb 中是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        //文件已经存在
        if(exists && optional.isPresent()){
            //文件已经存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //文件不存在 做一些准备工作  检查文件目录是否存在
        File fileFold = new File(fileFoldPath);
        if(!fileFold.exists()){
            fileFold.mkdirs();
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 获取文件所属目录的路径
     * @return
     */
    private String getFileFoldPath(String fileMd5){
        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/";
    }

    /**
     * 获取文件的路径  文件md5  文件扩展名
     * @param fileMd5 md5
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePath(String fileMd5,String fileExt){

        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileMd5+"."+fileExt;
    }

    /**
     * 得到分块文件
     * @param fileMd5
     * @param fileExt
     * @return
     */
    private String getChunkFileFoldPath(String fileMd5){

         return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/chunk/";
    }

    /**
     * 检查分块存储是否存在
     * @param fileMd5  文件md5
     * @param chunk 分块 下标
     * @param chunkSize  分块大小
     * @return
     */
    @Override
    public CheckChunkResult checkchuk(String fileMd5, Integer chunk, Integer chunkSize) {
       //得到分块目录
        String chunkFileFoldPath = getChunkFileFoldPath(fileMd5);
        File file=new File(chunkFileFoldPath+chunk);
        if(file.exists()){
            //文件存在
            return new CheckChunkResult(CommonCode.SUCCESS,true);
        }else{
            return new CheckChunkResult(CommonCode.SUCCESS,false);
        }
    }

    /**
     * 上传分块文件
     * @param file 文件
      * @param fileMd5 文件名
     * @param chunk 文件坐标
     * @return
     */
    @Override
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        //检查文件分块目录是否存在
        String chunkFileFoldPath = getChunkFileFoldPath(fileMd5);
        File file1=new File(chunkFileFoldPath);
        if(!file1.exists()){
            file1.mkdirs();
        }
        //开始存储  拿到上传文件的输入流
        InputStream inputStream=null;
        FileOutputStream fileOutputStream=null;
        try {
            inputStream = file.getInputStream();
            //分块文件路径
            fileOutputStream=new FileOutputStream(new File(chunkFileFoldPath+chunk));
           //流拷贝
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 文件合并
     * @param fileMd5 md5
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param mimetype 文件类型
     * @param fileExt  文件扩展名
     * @return
     */
    @Override
    public ResponseResult mergeChunk(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //合并所有的分块  //得到分块文件的所属目录
        String chunkFileFoldPath = getChunkFileFoldPath(fileMd5);
        File file=new File(chunkFileFoldPath);
        //拿到分块文件列表
        File[] files = file.listFiles();
        //转换
        List<File> list = Arrays.asList(files);
        //拿到文件路径
        String filePath = getFilePath(fileMd5, fileExt);
        //拿到合并文件
        File file1=new File(filePath);
        //创建一个合并文件
        File mergeFile = mergeFile(list, file1);
        if (mergeFile==null){
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        //校验前端传入的md5值与文件的md5一致  如果一致
        boolean b = checkFileMd5(mergeFile, fileMd5);
        //效验失败
        if(!b){
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        //将文件信息写入mongodb 数据库
        MediaFile mediaFile=new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        mediaFile.setFileOriginalName(fileName);
        //保存文件的相对路径
        String filePath1=fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/";
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");

        MediaFile save = mediaFileRepository.save(mediaFile);
        sendProcessVideoMsg(save.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 发送视屏消息处理
     * @param mediaId
     * @return
     */
    @Override
    public ResponseResult sendProcessVideoMsg(String mediaId) {
        //判断id是否正确
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //构建消息内容
        Map<String,String> map=new HashMap<>();
        map.put("mediaId",mediaId);
        String jsonString = JSON.toJSONString(map);
        //向mq发送视屏消息
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,jsonString);
        } catch (AmqpException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }


        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 校验md5
     * @param mergeFile 合并后的文件
     * @param md5 md5
     * @return
     */
    private boolean checkFileMd5(File mergeFile,String md5){
        //创建文件的输入流
        try {
            FileInputStream fileInputStream=new FileInputStream(mergeFile);
            //得到文件的md5  与传入的md5 对比
            String filemd5 = DigestUtils.md5Hex(fileInputStream);
            //忽略大小写比较
            if(md5.equalsIgnoreCase(filemd5)){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
            return false;
    }
    /**
     * 合并文件
     * @param files 文件列表
     * @param mergeFiles 原始文件
     * @return
     */
    private File mergeFile(List<File> files,File mergeFiles){
        //如果存在 就删除

        try {
            if(mergeFiles.exists()){
                mergeFiles.delete();
            }else {
                //创建新文件
                mergeFiles.createNewFile();
            }
            //对块文件进行排序
            Collections.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if(Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                        return 1;
                    }
                    return -1;
                }
            });
            //创建一个写对象
            RandomAccessFile randomAccessFile=new RandomAccessFile(mergeFiles,"rw");
           //创建一个缓存区
            byte [] bytes=new byte[1024];
            for (File file : files) {
                //读取
                RandomAccessFile read=new RandomAccessFile(file,"r");
                //创建一个下标
                int len=-1;
                //读写
                while ((len=read.read(bytes))!=-1){
                    randomAccessFile.write(bytes,0,len);
                }
                read.close();
            }
            randomAccessFile.close();
            return mergeFiles;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
