package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XcCompanyUserRepository extends JpaRepository<XcCompanyUser,String> {
    public XcCompanyUser findByUserId(String userId);
}
