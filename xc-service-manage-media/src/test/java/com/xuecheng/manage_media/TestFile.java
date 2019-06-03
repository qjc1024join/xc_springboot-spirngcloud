package com.xuecheng.manage_media;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*@SpringBootTest
@RunWith(SpringRunner.class)*/
public class TestFile {

    /**
     * 测试文件分块
     */
    @Test
    public void testFile() throws IOException {
        //源文件
        File file=new File("F:\\ffmpeg\\luncen.avi");
        //块文件目录
        String chunkFile="F:\\ffmpeg\\chunk\\";
        //先定义块文件的大小  1k  1m
        long chunkFilesize=1*1024*1024;
        //获取文件大小分块存储  块
        long chukFileNum= (long) Math.ceil(file.length() * 1.0/chunkFilesize);
        //读文件
        RandomAccessFile randomAccessFile=new RandomAccessFile(file,"r");
        //开始读取文件
        //缓存区
        byte [] bytes=new byte[1024];
        for (int i = 0; i <chukFileNum ; i++) {
            //存储到那个文件
            File file1=new File(chunkFile+i);
            //创建写对象
            RandomAccessFile randomAccessFile1=new RandomAccessFile(file1,"rw");
            int len=-1;
            while ((len=randomAccessFile.read(bytes))!=-1){
                //读取
                randomAccessFile1.write(bytes,0,len);
                //直到文件大小等于 1m  开始写下一块  下一次循环
                if(file1.length()==chunkFilesize){
                    break;
                }
            }
            randomAccessFile1.close();
        }
        randomAccessFile.close();
    }

    @Test
    public void merge() throws IOException {
        String FilePath="F:\\ffmpeg\\chunk\\";
        //创建块文件目录
        File file=new File(FilePath);
        //获取块文件列表
        File[] files = file.listFiles();
        //给文件排序
        List<File> list = Arrays.asList(files);
        //排序  制定排序规则
        Collections.sort(list, new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName())>=Integer.parseInt(o2.getName())){
                    //升序
                    return 1;
                }
                //降序
                return -1;
            }
        });
        //合并文件
        //源文件
        File fileMerge=new File("F:\\ffmpeg\\luncen_merge.avi");
        //创建新文件
        boolean newFile = fileMerge.createNewFile();
        //写文件
        RandomAccessFile randomAccessFile=new RandomAccessFile(fileMerge,"rw");
        byte [] bytes=new byte[1024];
        //开始读取
        for (File file1 : list) {
            //创建一个读块文件的对象
            RandomAccessFile readFile=new RandomAccessFile(file1,"r");
            int len=-1;
            while ((len=readFile.read(bytes))!=-1){
                //写入
                randomAccessFile.write(bytes,0,len);
            }
            readFile.close();
        }
        randomAccessFile.close();
    }
}
