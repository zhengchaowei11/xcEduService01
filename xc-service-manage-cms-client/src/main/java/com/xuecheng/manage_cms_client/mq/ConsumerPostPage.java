package com.xuecheng.manage_cms_client.mq;


import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class ConsumerPostPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerPostPage.class);

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    PageService pageService;


    @RabbitListener(queues={"${xuecheng.mq.queue}"} )
    public void postPage(String msg){
        Map map = JSON.parseObject(msg, Map.class);

        //获取页面的id
        String pageId = (String) map.get("pageId");

        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            LOGGER.error("receive cms post page,cmsPage is null:{}",msg.toString());
        }
        pageService.savePageToServerPath(pageId);

    }
}
