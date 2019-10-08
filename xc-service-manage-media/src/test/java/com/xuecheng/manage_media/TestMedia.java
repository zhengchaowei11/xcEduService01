package com.xuecheng.manage_media;


import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestMedia {
    @Test
    public void testChunk() throws IOException{
        File sourceFile =  new File("F:/develop/video/test/lucene.avi");
        String chunkPath = "F:/develop/video/test1/";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()){
            //创建文件夹
            chunkFolder.mkdirs();
        }

        //分块大小
        long chunkSize = 1*1024*1024;
        //分块数量 sourceFile 如果不存在，就创造  分块的数量
        long chunkNum = (long)Math.ceil(sourceFile.length()* 1.0 / chunkSize);

        if (chunkNum <0){
            chunkNum =1;
        }
        //缓存区大小
        byte[] b = new byte[1024];
        //使用RandomAccessFile访问文件 进行读文件
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile,"r");

        for (int i=0;i<chunkNum;i++){
            //创建分块文件
                File file = new File(chunkPath+i);
                boolean newFile = file.createNewFile();
                if (newFile){
                    //随机读取文件的类

                    RandomAccessFile raf_write = new RandomAccessFile(file,"rw");
                    int len = -1;
                    //跳出循环，不进行其他的操作
                    while((len = raf_read.read(b)) != -1){
                        raf_write.write(b,0,len);
                        if (file.length() > chunkSize){
                            break;
                        }
                    }
                    raf_write.close();
                }


        }
        raf_read.close();

    }


    @Test
    public void testMerge() throws IOException{
        //块文件的目录
        File chunkFolder = new File("F:/develop/video/test1/");
        //合并文件
        File mergeFile = new File("F:/develop/video/test/lucene1.avi");
        if (mergeFile.exists()){
            mergeFile.delete();
        }
        mergeFile.createNewFile();

        //用于写文件的
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");


        //设置缓冲区
        byte[] b = new byte[1024];
        //分块列表  ,文件数组，其中的排序是不按照指定的顺序排序  块文件列表
        File[] files = chunkFolder.listFiles();
        //将块文件排序，按名称排序
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName()))
                {
                    return 1;
                }
                return  -1;
            }
        });
        //合并文件   在for中的定义变量只在大括号中起到作用，在其他范围起不了作用，但是变量名在方法中只能有一个，不能起同样的变量名
        int len = -1 ;
        for (File file : fileList){
            //读文件，之后写文件
            RandomAccessFile raf_read = new RandomAccessFile(file,"r");
            while ((len = raf_read.read(b)) != -1){
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }

        raf_write.close();








    }
}
