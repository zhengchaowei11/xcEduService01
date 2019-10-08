package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

public interface FileSystemControllerApi {
    @ApiOperation("文件上传的入口")
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata);

}
