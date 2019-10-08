package com.xuecheng.framework.domain.cms.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CmsPostPageResult extends ResponseResult {
    String pageUrl;
    public CmsPostPageResult( ResultCode resultCode, String pageUrl){
        //super  必须是在第一行的，调用父类的构造方法
        super(resultCode);
        this.pageUrl = pageUrl;
    }
}
