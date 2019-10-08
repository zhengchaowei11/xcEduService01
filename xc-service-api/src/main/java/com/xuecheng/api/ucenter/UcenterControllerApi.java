package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;

public interface UcenterControllerApi {
    public XcUserExt getXcUserExt(String userName);
}
