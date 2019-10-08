package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.procedure.spi.ParameterRegistrationImplementor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {
    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    private String upload_location;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;


    //得到文件所在的文件夹的目录

    //变量只能用下划线，来连接
    private String getFileFolderPath(String fileMd5){
        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/";
    }
    //得到文件的路径
    private String getFilePath(String fileMd5,String fileExt){
        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileMd5+"."+fileExt;
    }

    //得到块文件的路径
    private String getChunkFileFolderPath(String fileMd5){
        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/chunk/";
    }


    //进行文件的检查，检查是不是存在
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //检查文件是不是存在服务器中，在查找文件的Mongodb中的文件信息是否存在
        String filePath = this.getFilePath(fileMd5, fileExt);
        String fileFolderPath = this.getFileFolderPath(fileMd5);

        File file = new File(filePath);
        boolean exists = file.exists();

        Optional<MediaFile> optionalFile = mediaFileRepository.findById(fileMd5);
        if (exists && optionalFile.isPresent()){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //文件夹的目录是不是存在
        File file1 = new File(fileFolderPath);
        if (!file1.exists()){
            file1.mkdirs();
        }
        return new ResponseResult(CommonCode.SUCCESS);

    }

    //检查分块的文件
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        String chunkFileFolder = this.getChunkFileFolderPath(fileMd5);
        File file = new File(chunkFileFolder+chunk);
        if (file.exists()){
            return new CheckChunkResult(CommonCode.SUCCESS,true);
        }else {
            return new CheckChunkResult(CommonCode.SUCCESS,false);
        }
    }

    //分块文件的上传
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {
        //检查分块文件的路径，如果分块的文件不存在的话，就进行创建
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //块文件的路径
        String filePath = this.getChunkFileFolderPath(fileMd5) + chunk;

        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()){
            //创建文件的目录
            chunkFileFolder.mkdirs();
        }
        //进行文件的上传

        InputStream inputStream = null;
        //文件输出流
        FileOutputStream fileOutputStream = null;

        try {
             inputStream = file.getInputStream();
             fileOutputStream = new FileOutputStream(new File(filePath));
             IOUtils.copy(inputStream,fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     *
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     *
     * 思路:说明分块我文件的上传都成功了
     */
    //分块文件的合并
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //得到分块文件的路径
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        File[] files = chunkFileFolder.listFiles();
        //得到文件的列表
        List<File> fileList = Arrays.asList(files);


        //得到合并文件的路径
        String filePath = this.getFilePath(fileMd5, fileExt);
        File margeFile = new File(filePath);

        //得到mergeFile文件
        File mergeFile = this.mergeFile(fileList, margeFile);
        if (margeFile == null){
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        //判断md5的文件是不是合格
        boolean checkFileMd5 = this.checkFileMd5(margeFile, fileMd5);
        if (!checkFileMd5){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        //将文件保存到指定的mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5 + "." +fileExt);
        //文件路径保存相对路径
        String filePath1 = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);
        sendProcessVideoMsg(fileMd5);
        return new ResponseResult(CommonCode.SUCCESS);


    }

    private boolean checkFileMd5(File margeFile,String fileMd5){
        if (margeFile == null || StringUtils.isEmpty(fileMd5)){
            return false;
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(margeFile);
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            if (md5Hex.equalsIgnoreCase(fileMd5)){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private File mergeFile(List<File> fileList,File margeFile){

        //如果进行try 的话，其余的代码尽量写在这里面
        try {
            if (margeFile.exists()){
                margeFile.delete();
            }else {
                margeFile.createNewFile();
            }

            //对fileList进行排序
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                        return 1;
                    }
                    return -1;
                }
            });

            RandomAccessFile rad_write = new RandomAccessFile(margeFile,"rw");
            byte[] bytes = new byte[1024];
            for (File chunkFile : fileList){
                RandomAccessFile rad_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                while ((len = rad_read.read(bytes)) != -1){
                    rad_write.write(bytes,0,len);
                }
                rad_read.close();
            }
            rad_write.close();
            return margeFile;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }




    }

    private ResponseResult sendProcessVideoMsg(String mediaId){
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()){
            return new ResponseResult(CommonCode.FAIL);
        }
        Map<String,String> map = new HashMap<>();
        map.put("mediaId",mediaId);
        String message = JSON.toJSONString(map);
        //主动加try{} catch ,如果发生了错误 ，可以通过try{} catch(){}来实现，如果发送消息失败，返回一个操作失败的结果
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,message);
        } catch (AmqpException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

}
