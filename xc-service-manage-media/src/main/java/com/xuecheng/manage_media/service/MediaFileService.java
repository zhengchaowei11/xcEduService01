package com.xuecheng.manage_media.service;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaFileService {
    @Autowired
    MediaFileRepository mediaFileRepository;

    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
        //防止出现空指针的问题
        if (queryMediaFileRequest == null){
            queryMediaFileRequest = new QueryMediaFileRequest();
        }
        if (page < 0){
            page = 1;
        }
        page = page - 1;
        if (size < 0){
            size = 10;
        }
        Pageable pageable = PageRequest.of(page,size);
        MediaFile mediaFile = new MediaFile();
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getTag())){
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())){
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())){
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("tags", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains());//tag字段进行模糊匹配查询
        Example<MediaFile> example = Example.of(mediaFile,exampleMatcher);
        Page<MediaFile> filePage = mediaFileRepository.findAll(example, pageable);
        //数据的总记录数
        long totalElements = filePage.getTotalElements();
        List<MediaFile> content = filePage.getContent();
        QueryResult<MediaFile> queryResult = new QueryResult<>();
        queryResult.setList(content);
        queryResult.setTotal(totalElements);
        QueryResponseResult<MediaFile> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

}
