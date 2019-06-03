package com.xuecheng.manage_course.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import com.xuecheng.manage_course.service.CourseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceimpl implements CourseService{
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    private TeachplanRepository teachplanRepository;
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    CourseMarketRepository  courseMarketRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    @Autowired
    CoursePubRepositoy coursePubRepositoy;

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;
    @Value("${course‐publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course‐publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course‐publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course‐publish.siteId}")
    private String publish_siteId;
    @Value("${course‐publish.templateId}")
    private String publish_templateId;
    @Value("${course‐publish.previewUrl}")
    private String previewUrl;
    @Override
    public TeachplanNode findTeachplanList(String courseid) {
        return teachplanMapper.selectList(courseid);
    }

    /**
     * 更新课程数据
     * @param teachplan  课计划数据
     * @return
     */
    @Override
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        //判断参数是否违法
        if(teachplan==null || StringUtils.isEmpty(teachplan.getCourseid())||StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALITADE);
        }
        //获取课程id
        String courseid = teachplan.getCourseid();
        //获取父级节点id
        String parentid = teachplan.getParentid();
        if(StringUtils.isEmpty(parentid)){
            //取出当前课程的根节点
            parentid = findTeachplan(courseid);
        }
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan teachplan1 = optional.get();
        String parentid1 = teachplan1.getGrade();

        Teachplan teachplanNew=new Teachplan();
        //将用户输入的添加到课程里面
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setCourseid(courseid);
        teachplanNew.setParentid(parentid);
        if(parentid1.equals("1")){
            teachplanNew.setGrade("2");
        }else{
            teachplanNew.setGrade("3");
        }
        teachplanRepository.save(teachplanNew);
        return new ResponseResult(CommonCode.SUCCESS);
    }



    /**
     * 根据课程id 查询  如果查询不到则自动添加
     * @param courseid  课程id
     * @return
     */
    private String findTeachplan(String courseid){
        //根据课程id查询
        Optional<CourseBase> optional = courseBaseRepository.findById(courseid);
        if(!optional.isPresent()){
            return null;
        }
        //拿到课程对象
        CourseBase courseBase = optional.get();
        //查询课程根节点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndAndParentid(courseid, "0");
       //如果查询不到 则添加根节点
        if(teachplanList==null||teachplanList.size()<=0){
            Teachplan teachplan=new Teachplan();
            //设置为父级id
            teachplan.setParentid("0");
            //设置级别
            teachplan.setGrade("1");
            //设置课程名称
            teachplan.setPname(courseBase.getName());
            //设置课程id
            teachplan.setCourseid(courseid);
            //设置发布状态
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        return teachplanList.get(0).getId();
    }



    /**
     *
     * @param page 当前页
     * @param size 每页显示数量
     * @param courseListRequest  扩展功能
     * @return
     */
    @Override
    public QueryResponseResult findCourseList(String company_id,int page, int size, CourseListRequest courseListRequest) {
       if(courseListRequest==null){
           courseListRequest=new CourseListRequest();
       }
       if(!StringUtils.isEmpty(company_id)){
           courseListRequest.setCompanyId(company_id);
       }

       if(page<=0){
           page=0;
       }
       if(size<=0){
           size=20;
       }
       //设置分页参数   会启动一个线程
        PageHelper.startPage(page,size);
        //执行分页查询
        Page<CourseInfo> courseList = courseMapper.findCourseList(courseListRequest);
        //获取参数  获取列表
        List<CourseInfo> result = courseList.getResult();
        //获取总记录数
        long total = courseList.getTotal();
        //将数据进行封装
        QueryResult<CourseInfo> courseIncfoQueryResult = new QueryResult<CourseInfo>();
        courseIncfoQueryResult.setList(result);
        courseIncfoQueryResult.setTotal(total);
        return new QueryResponseResult(CommonCode.SUCCESS, courseIncfoQueryResult);
    }

    @Override
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        //课程状态默认为未发布
        System.out.println(courseBase);
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    @Override
    public CourseBase getCoursebaseById(String courseid) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseid);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseResult updateCoursebase(String id, CourseBase courseBase) {
        CourseBase one = this.getCoursebaseById(id);
        if(one == null){
//抛出异常..
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
//修改课程信息
        one.setName(courseBase.getName());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setUsers(courseBase.getUsers());
        one.setDescription(courseBase.getDescription());
        CourseBase save = courseBaseRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Override
    public CourseMarket getCourseMarketById(String courseid) {
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseid);
        if(!optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Override
    @Transactional
    public CourseMarket updateCourseMarket(String id, CourseMarket courseMarket) {
        CourseMarket one = this.getCourseMarketById(id);
        if(one!=null){
            one.setCharge(courseMarket.getCharge());
            one.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
            one.setEndTime(courseMarket.getEndTime());//课程有效期，结束时间
            one.setPrice(courseMarket.getPrice());
            one.setQq(courseMarket.getQq());
            one.setValid(courseMarket.getValid());
            courseMarketRepository.save(one);
        }else{
//添加课程营销信息
            one = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, one);
//设置课程id
            one.setId(id);
            courseMarketRepository.save(one);
        }
        return one;
    }

    /**
     * 添加课程图片与课程关联的关系
     * @param courseId
     * @param pic
     * @return
     */
    @Override
    @Transactional
    public ResponseResult addCoursepic(String courseId, String pic) {
        /**
         * 课程图片的信息
         */
        CoursePic coursePic=null;
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if(optional.isPresent()){
           coursePic=optional.get();
        }
        if(coursePic==null){
            coursePic=new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Override
    public CoursePic findCoursePic(String courseid) {
        Optional<CoursePic> optional =
                coursePicRepository.findById(courseid);
        if(optional.isPresent()){
            CoursePic coursePic = optional.get();
            return coursePic;
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        long deleteByCourseid = coursePicRepository.deleteByCourseid(courseId);
        if(deleteByCourseid>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 查询课程的视图  图片
     * @param id
     * @return
     */
    @Override
    public CourseView getCourseView(String id) {
        CourseView courseView=new CourseView();
        /*课程基本信息*/
        Optional<CourseBase> courseBase = courseBaseRepository.findById(id);
        if(courseBase.isPresent()){
            CourseBase courseBase1 = courseBase.get();
            courseView.setCourseBase(courseBase1);
        }
        /*课程图片*/
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(id);
        if(optionalCoursePic.isPresent()){
            CoursePic coursePic = optionalCoursePic.get();
            courseView.setCoursePic(coursePic);
        }
        /*课程营销信息*/
        Optional<CourseMarket> optionalCourseMarket = courseMarketRepository.findById(id);
        if(optionalCourseMarket.isPresent()){
            CourseMarket courseMarket = optionalCourseMarket.get();
            courseView.setCourseMarket(courseMarket);
        }
        /*课程计划信息*/
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        return null;
    }
            /**
     * 课程预览
     * @param id
     * @return
     */
    @Override
    public CoursePublishResult preview(String id) {
        //根据课程id查询课程
        CourseBase baseById = findCourseBaseById(id);
        //请求cms 添加页面  //远程调用fine
        //定义cmspage 的信息
        CmsPage cmsPage=new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setDataUrl(publish_dataUrlPre+id);
        cmsPage.setPageName(id+".html");
        cmsPage.setPageAliase(baseById.getName());
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);
        //远程请求cmspage 拿到页面信息
        CmsPageResult save = cmsPageClient.save(cmsPage);
        //拼装页面预览的url
        if(!save.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //如果拿到 就获取页面信息
        CmsPage cmsPage1 = save.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //返回页面预览的url
        String previewUr=previewUrl+pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,previewUr);
    }

    /**
     * 课程发布
     * @param id
     * @return
     */
    @Override
    @Transactional
    public CoursePublishResult publih(String id) {
        //根据课程id查询课程
        CourseBase baseById = findCourseBaseById(id);
        //调用cms 一键发布接口的将课程详情页面发布到服务
        CmsPage cmsPage=new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setDataUrl(publish_dataUrlPre+id);
        cmsPage.setPageName(id+".html");
        cmsPage.setPageAliase(baseById.getName());
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);
        CmsPostPageResult pageResult = cmsPageClient.postPageQuick(cmsPage);
        if(!pageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //保存课程的发布状态为已发布
        CourseBase courseBase = saveCoursePubState(id);
        if(courseBase==null){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //保存课程索引信息
        //先创建一个coursePub对象
        CoursePub coursePub = createCoursePub(id);
        //将coursePub对象保存到数据库
        saveCoursePub(id,coursePub);
        //缓存课程的信息
        //...

        //保存课程索引的信息  缓存的信息
        String pageUrl = pageResult.getPageUrl();
        //根据课程id保存课程记录
        saveTeachplanMediaPub(id);
        //向teachplanMedia中添加 记录
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    /**
     * 根据课程id 保存记录信息
     * @param courseId
     */
    private void saveTeachplanMediaPub(String courseId){
        //查询课程媒资信息
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
//将课程计划媒资信息存储待索引表
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for(TeachplanMedia teachplanMedia:teachplanMediaList){
            TeachplanMediaPub teachplanMediaPub =new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }


    //将coursePub对象保存到数据库
    private CoursePub saveCoursePub(String id,CoursePub coursePub){

        CoursePub coursePubNew = null;
        //根据课程id查询coursePub
        Optional<CoursePub> coursePubOptional = coursePubRepositoy.findById(id);
        if(coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }else{
            coursePubNew = new CoursePub();
        }

        //将coursePub对象中的信息保存到coursePubNew中
        BeanUtils.copyProperties(coursePub,coursePubNew);
        coursePubNew.setId(id);
        //时间戳,给logstach使用
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);
        coursePubRepositoy.save(coursePubNew);

        return coursePubNew;
    }
    //创建coursePub对象
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        //根据课程id查询course_base
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(id);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            //将courseBase属性拷贝到CoursePub中
            BeanUtils.copyProperties(courseBase,coursePub);
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }

        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }

        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String jsonString = JSON.toJSONString(teachplanNode);
        //将课程计划信息json串保存到 course_pub中
        coursePub.setTeachplan(jsonString);
        return coursePub;

    }

    /**
     * 更改课程发布状态
     * @return
     */
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBase = findCourseBaseById(courseId);
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }
    /**
     * 课程计划与媒资之间的关联
     * @param teachplanMedia
     * @return
     */
    @Override
    public ResponseResult saveMedia(TeachplanMedia teachplanMedia) {
        if(teachplanMedia==null||StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.cast(CommonCode.INVALITADE);
        }
        //效验课程计划是否为三级目录
        String teachplanId = teachplanMedia.getTeachplanId();
        //查询课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALITADE);
        }
        //查询到教学计划
        Teachplan teachplan = optional.get();
        //取出等级
        String grade = teachplan.getGrade();
        if(StringUtils.isEmpty(grade) || !grade.equals("3")){
            //只允许等级三的关联视屏
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        //查询
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia teachplanMedia1=null;
        if(mediaOptional.isPresent()){
            teachplanMedia1 = mediaOptional.get();
        }else {
            teachplanMedia1=new TeachplanMedia();
        }
        // teachplanMedia1 更新到数据库
        teachplanMedia1.setCourseId(teachplan.getCourseid());
        //文件id
        teachplanMedia1.setMediaId(teachplanMedia.getMediaId());
        //媒资文件的名称
        teachplanMedia1.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        //url
        teachplanMedia1.setMediaUrl(teachplanMedia.getMediaUrl());
        //教学计划的id
        teachplanMedia1.setTeachplanId(teachplanId);
        TeachplanMedia save = teachplanMediaRepository.save(teachplanMedia1);
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
