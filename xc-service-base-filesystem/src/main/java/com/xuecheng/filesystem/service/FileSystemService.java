package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileSystemService {


    //把配置文件的信息注入
    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    String charset;


    @Autowired
    FileSystemRepository fileSystemRepository;


    //上传文件到fsfs ,返回id
    public UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata) {
        if(multipartFile == null){
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }

        String fileId = fdfs_upload(multipartFile);
        FileSystem fileSystem = new FileSystem();
        fileSystem.setBusinesskey(businesskey);
        fileSystem.setFiletag(filetag);
        fileSystem.setFileId(fileId);
        fileSystem.setFilePath(multipartFile.getName());
        fileSystem.setFileName(multipartFile.getName());
        fileSystem.setFileSize(multipartFile.getSize());
        fileSystem.setFileType(multipartFile.getContentType());
        if (StringUtils.isNotEmpty(metadata)){
            JSON.parseObject(metadata, Map.class);
        }
        fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS,fileSystem);

    }

    private String  fdfs_upload(MultipartFile multipartFile) {

        try {
            //加载fdfs
            initFdfsConfig();
            //得到TrackerClient的客户端
            TrackerClient trackerClient = new TrackerClient();
            //得到trackerServer的客户端
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storageServer);
            //转化成字节数组
            byte[] bytes = multipartFile.getBytes();
            //文件的原始名称
            String originalFilename = multipartFile.getOriginalFilename();
            //文件的扩展名
            //获取以点开头的字符的位置 ，然后加一，然后就进行截取的操作
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

            String file1 = storageClient1.upload_file1(bytes, extName, null);
            System.out.println(file1);
            return file1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //加载fdfs的配置
    private void initFdfsConfig(){
        try {
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
            ClientGlobal.setG_charset(charset);
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
        }
    }
}
