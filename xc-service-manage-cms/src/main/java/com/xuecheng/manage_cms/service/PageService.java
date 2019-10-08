package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueyPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {
    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private CmsConfigRepository cmsConfigRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    //Service 中的方法一般要跟controller方法保持一致
    public QueryResponseResult findList( int page,int size, QueyPageRequest queyPageRequest) {
        //根据条件查询的方法 不能为空，如果为空的话，下面的操作可能会出现空指针的异常
        if(queyPageRequest == null){
            queyPageRequest = new QueyPageRequest();
        }
        //自定义条件查询
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        //创造查询条件的 进行精准的匹配查询
        CmsPage cmsPage = new CmsPage();
        //如果得到的站点id不是空的话，那么就进行添加，此处用到lang3包中的判断方法
        if(StringUtils.isNotEmpty(queyPageRequest.getSiteId())){
            cmsPage.setSiteId(queyPageRequest.getSiteId());
        }
        //根据页面的别名来进行查询
        if(StringUtils.isNotEmpty(queyPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queyPageRequest.getPageAliase());
        }

        //定义条件对象
        Example<CmsPage>example = Example.of(cmsPage,exampleMatcher);


        if(page<=0){
            page =1;
        }
        page = page - 1;
        if(size<=0){
            size=10;
        }
        //封装一个Pageable 对象
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);
        //查询的结果
        QueryResult<CmsPage> queryResult = new QueryResult<>();
        queryResult.setTotal(all.getTotalElements());//数据的总记录数
        queryResult.setList(all.getContent());//数据列表 应该是一页的

        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    //新增页面
    public CmsPageResult add(CmsPage cmsPage){
        //主要是让从数据库中查询数据
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(cmsPage1 != null){
            //查询不出数据，就进行添加的操作
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        cmsPage.setPageId(null);
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);


    }

    //根据用户的id进行查询
    public CmsPage getId(String id){
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    //进行页面的修改
    public CmsPageResult update(String id, CmsPage cmsPage){
        CmsPage one = this.getId(id);

        if(one != null){
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataUrl
            one.setDataUrl(cmsPage.getDataUrl());
            //执行更新
            CmsPage save = cmsPageRepository.save(one);
            if(save != null){
                return new CmsPageResult(CommonCode.SUCCESS,save);
            }
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    //进行页面的删除
    public ResponseResult deleteById(String id){
        //判断这个id
        //删除之前先进行查询
        CmsPage one = this.getId(id);
        if(one != null){
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    public CmsConfig getConfigById(String id){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if(optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }

    //页面静态化方法
    public String getPageHtml(String pageId){
        //获取数据模型
        Map model = getModelByPageId(pageId);
        if(model == null){
            //数据模型获取不到
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取页面模板
        String template = getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(template)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        String html = generateHtml(template, model);
        return html;

    }

    private String generateHtml(String template,Map model) {
        try {
            Configuration configuration = new Configuration(Configuration.getVersion());
//模板加载器
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template", template);
//配置模板加载器
            configuration.setTemplateLoader(stringTemplateLoader);
//获取模板
            Template template1 = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取页面的模板
    private String getTemplateByPageId(String pageId){
        CmsPage cmsPage = this.getId(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //获取页面模板的id
        String templateId = cmsPage.getTemplateId();
        if(templateId.isEmpty()){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        Optional<CmsTemplate> optional  = cmsTemplateRepository.findById(templateId);
        if(optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            String templateFileId = cmsTemplate.getTemplateFileId();
            //根据id查询文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开一个下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);

            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //获取数据模型
    private Map getModelByPageId(String pageId){
        //取出页面的信息
        CmsPage cmsPage = this.getId(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出页面的dataUrl
        String dataUrl = cmsPage.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过restTemplate请求dataUrl获取数据  进行远程的请求的操作
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }


    public ResponseResult postPage(String pageId){
        //执行页面的静态化
        String pageHtml = this.getPageHtml(pageId);
        if(StringUtils.isEmpty(pageHtml)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //保存静态化的页面
        CmsPage cmsPage = this.saveHtml(pageHtml, pageId);
        //发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);

    }

    private void sendPostPage(String pageId){
        CmsPage cmsPage = this.getId(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        Map<String,String> map = new HashMap<>();
        map.put("pageId",pageId);
        String jsonString = JSON.toJSONString(map);
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,cmsPage.getSiteId(),jsonString);


    }
    //将页面保存到cmsGrid中
    private CmsPage saveHtml(String pageHtml,String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();
        InputStream inputStream = null;
        ObjectId objectId = null;
        try {
            inputStream = IOUtils.toInputStream(pageHtml, "UTF-8");
            objectId = gridFsTemplate.store(inputStream, cmsPage.getSiteId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //进行页面的gridfs的保存的操作
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;

    }

    public CmsPageResult save(CmsPage cmsPage) {
        //通过查询页面的唯一索引
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 != null){
            return this.update(cmsPage1.getPageId(),cmsPage);
        }
        return this.add(cmsPage);
    }


    private CmsSite findCmsSiteById(String cmsSiteId){
        Optional<CmsSite> optionalSite = cmsSiteRepository.findById(cmsSiteId);
        if (optionalSite.isPresent()){ 
            return optionalSite.get();
        }
        return null;
    }

    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //保存页面
        CmsPageResult cmsPageResult = this.save(cmsPage);
        if (!cmsPageResult.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        //  调用发布页面的方法
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        ResponseResult responseResult = this.postPage(pageId);
        if (!responseResult.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }

        String siteId = cmsPage.getSiteId();
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        String siteDomain = cmsSite.getSiteDomain();
        String siteWebPath = cmsSite.getSiteWebPath();
        String pageWebPath = cmsPage1.getPageWebPath();
        String pageName = cmsPage1.getPageName();
        String pageUrl = siteDomain + siteWebPath + pageWebPath + pageName ;
        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);
    }
}

