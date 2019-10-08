package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    XcUserRepository xcUserRepository;
    
    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;

    @Autowired
    XcMenuMapper xcMenuMapper;

    public XcUser findXcUserByUserName(String userName){
        return xcUserRepository.findByUsername(userName);
    }
    
    public XcUserExt getXcUserExt(String userName) {
        XcUser xcUser = this.findXcUserByUserName(userName);
        if (xcUser == null){
            return null;
        }

        XcUserExt xcUserExt = new XcUserExt();
        //文件的拷贝
        BeanUtils.copyProperties(xcUser,xcUserExt);
        String userId = xcUserExt.getId();
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);
        if (xcMenus != null){
            xcUserExt.setPermissions(xcMenus);
        }
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(userId);
        if (xcCompanyUser != null){
            String companyId = xcCompanyUser.getCompanyId();
            xcUserExt.setCompanyId(companyId);
        }
        return xcUserExt;
    }

}
