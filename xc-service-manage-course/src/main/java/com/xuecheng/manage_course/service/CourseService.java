package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Autowired
    CourseMapper courseMapper;

    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    public TeachplanNode findTeachplanList(String courseId){
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        System.out.println(teachplanNode);
        return teachplanNode;
    }

    public ResponseResult addTeachplan(Teachplan teachplan) {
        //获取课程的id信息
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid())
                || StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //取出课程的id
        String courseid = teachplan.getCourseid();
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)){
            parentid = this.getTeachplanRoot(courseid);
        }
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        //不用进行判断了，因为返回的对象永远不会为空
        Teachplan teachplanParent = optional.get();
        String grade = teachplanParent.getGrade();
        Teachplan teachplanNew = new Teachplan();
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setCourseid(courseid);
        teachplanNew.setParentid(parentid);
        if(grade.equals("1")){
            teachplanNew.setGrade("2");
        }else {
            teachplanNew.setGrade("3");
        }
        teachplanRepository.save(teachplanNew);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private String getTeachplanRoot(String courseid){
        if (StringUtils.isEmpty(courseid)){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        Optional<CourseBase> optional = courseBaseRepository.findById(courseid);
        if (!optional.isPresent()){
            return null;
        }
        CourseBase courseBase = optional.get();
        List<Teachplan> teachplanNodes = teachplanRepository.findByCourseidAndParentid(courseid, "0");
        if (teachplanNodes==null || teachplanNodes.size()<= 0 ){
            //如果查不到的话，这个课程可能是新添加的，还没有进行上线  需要自己到课程表的基本信息里面查找

            Teachplan teachplanRoot = new Teachplan();
            teachplanRoot.setPname(courseBase.getName());
            teachplanRoot.setGrade("1");
            teachplanRoot.setCourseid(courseid);
            teachplanRoot.setStatus("0");
            teachplanRoot.setParentid("0");
            teachplanRepository.save(teachplanRoot);
            return teachplanRoot.getId();
        }
        //这个集合一共就一个节点
        return teachplanNodes.get(0).getId();
    }
    @Transactional
    public ResponseResult addCourse(String courseId, String pic) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (optional.isPresent()){
            coursePic = optional.get();
        }
        if (coursePic == null){
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    @Transactional
    public CoursePic findCoursePic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (optional.isPresent()){
            return coursePic = optional.get();
        }
        return null;
    }

    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        Long result = coursePicRepository.deleteByCourseid(courseId);
        if (result > 0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    public CourseView getCoruseView(String id) {
        CourseView courseView = new CourseView();
        Optional<CourseBase> optionalBase = courseBaseRepository.findById(id);
        if (optionalBase.isPresent()){
            CourseBase courseBase = optionalBase.get();
            courseView.setCourseBase(courseBase);
        }
        Optional<CoursePic> optional = coursePicRepository.findById(id);
        if (optional.isPresent()){
            CoursePic coursePic = optional.get();
            courseView.setCoursePic(coursePic);
        }
        Optional<CourseMarket> optionalCourseMarket = courseMarketRepository.findById(id);
        if (optionalCourseMarket.isPresent()){
            CourseMarket courseMarket = optionalCourseMarket.get();
            courseView.setCourseMarket(courseMarket);
        }

        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(courseId);
        if (optionalCourseBase.isPresent()){
            CourseBase courseBase = optionalCourseBase.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_DENIED_DELETE);
        return null;

    }
    //课程预览
    public CoursePublishResult preview(String id) {
        CourseBase courseBase = this.findCourseBaseById(id);

        //进行远程的调用方法 使用fegin   调用远程的方法相当于调用本地的方法
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);  //站点id
        cmsPage.setDataUrl(publish_dataUrlPre+id);  //数据模型
        cmsPage.setPageName(id+".html");
        cmsPage.setPageAliase(courseBase.getName());
        cmsPage.setPageWebPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);


        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if (!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        String url = previewUrl+pageId;

        return new CoursePublishResult(CommonCode.SUCCESS,url);
    }

    @Transactional
    public CoursePublishResult publish(String id) {
        //调用远程的方法，实现远程方法的调用
        CourseBase courseBase = this.findCourseBaseById(id);
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);  //站点id
        cmsPage.setDataUrl(publish_dataUrlPre+id);  //数据模型
        cmsPage.setPageName(id+".html");
        cmsPage.setPageAliase(courseBase.getName());
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);

        CoursePub coursePub = this.createCoursePub(id);

        CoursePub coursePub1 = save(id, coursePub);
        if (coursePub1 == null){
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_VIEWERROR);
        }

        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        CourseBase courseBase1 = this.saveCoursePubState(id);
        if (courseBase1 == null){
            return new CoursePublishResult(CommonCode.FAIL , null);
        }

        //保存课程索引信息
        //先创建一个CoursePub对象
        saveTeachplanMediaPub(id);


        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    /**
     * 思路：
     * 主要是为了向logstash中创建索引
     * 1.先删除TeachplanMediaPub中的数据
     * 2.根据课程的id查找TeahchMedia中的多条数据，然后拷贝到 TeachMediaPub中，添加时间撮、
     *用到的知识点是saveAll(数组)   一个数组向另外一个数据的过程
     */

    public void saveTeachplanMediaPub(String courseId){
        //查询课程的信息
        teachplanMediaPubRepository.deleteByCourseId(courseId);
         List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
         //向课程中添加信息   向查询信息，结果是一个数据
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        for (TeachplanMedia teachplanMedia : teachplanMediaList ){
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubs.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubs);

    }



    //课程的页面的状态的改变
    public CourseBase saveCoursePubState(String courseId){
        CourseBase courseBase = this.findCourseBaseById(courseId);
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }

    private CoursePub  createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        //课程基本信息保存
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(id);
        if (optionalCourseBase.isPresent()){
            CourseBase courseBase = optionalCourseBase.get();
            BeanUtils.copyProperties(courseBase,coursePub);
        }
        //将课程图片保存到CoursePub
        Optional<CoursePic> coursePicRepositoryById = coursePicRepository.findById(id);
        if (coursePicRepositoryById.isPresent()){
            CoursePic coursePic = coursePicRepositoryById.get();
            BeanUtils.copyProperties(coursePic,coursePub);
        }
        //将课程的营销信息保存到CoursePub
        Optional<CourseMarket> courseMarketRepositoryById = courseMarketRepository.findById(id);
        if (courseMarketRepositoryById.isPresent()){
            CourseMarket courseMarket = courseMarketRepositoryById.get();
            BeanUtils.copyProperties(courseMarket,coursePub);
        }


        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);
        return coursePub;
    }

    private CoursePub save(String id,CoursePub coursePub){
        CoursePub coursePubNew = null;

        Optional<CoursePub> optionalPub = coursePubRepository.findById(id);
        if (optionalPub.isPresent()){
            coursePubNew = optionalPub.get();
        }else {
            coursePubNew = new CoursePub();
        }
        //将coursePub保存到courseNew
        BeanUtils.copyProperties(coursePub,coursePubNew);
        coursePubNew.setId(id);
        coursePubNew.setTimestamp(new Date());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       /* String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);*/
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }

    /**
     * 思路：
     * @param teachplanMedia
     * @return
     */

    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {

        if (teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Optional<Teachplan> optionalTeachplan = teachplanRepository.findById(teachplanMedia.getTeachplanId());
        if (!optionalTeachplan.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        //教学计划
        Teachplan teachplan = optionalTeachplan.get();
        String grade = teachplan.getGrade();
        if (StringUtils.isEmpty(grade) || !(teachplan.getGrade()).equals("3")){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanMedia.getTeachplanId());
        TeachplanMedia one = null;
        //这样的选择是为了更好的，更新或者的保存信息的使用
        //是为空的表现
        if (!teachplanMediaOptional.isPresent()){
            one = new TeachplanMedia();
        }else {
            one = teachplanMediaOptional.get();
        }
        one.setCourseId(teachplanMedia.getCourseId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        one.setTeachplanId(teachplanMedia.getTeachplanId());
        teachplanMediaRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //company_id 的参数是从jwt中请求过来的
    public QueryResponseResult<CourseInfo> findCourseList(String company_id,int page, int size, CourseListRequest courseListRequest) {
       //设置查询的条件
        if (courseListRequest == null){
            courseListRequest = new CourseListRequest();
            courseListRequest.setCompanyId(company_id);
        }
        courseListRequest.setCompanyId(company_id);
        if(page <= 0){
            page = 0;
        }
        if (size <= 0){
            size = 20;
        }
        PageHelper.startPage(page,size);
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        long total = courseListPage.getTotal();
        List<CourseInfo> result = courseListPage.getResult();
        QueryResult<CourseInfo> queryResult = new QueryResult();
        queryResult.setList(result);
        queryResult.setTotal(total);
        return new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
    }
}
