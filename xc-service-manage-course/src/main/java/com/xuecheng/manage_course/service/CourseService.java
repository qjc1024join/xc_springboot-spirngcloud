package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface CourseService {
    /**
     * 根据课程id 查询节点
     * @param courseid 课程id
     * @return
     */
    TeachplanNode findTeachplanList(String courseid);

    /**
     * 添加课程计划
     * @param teachplan  课计划数据
     * @return  返回数据信息
     */
    ResponseResult addTeachplan(Teachplan teachplan);

    /**
     * 查询课程
     * @param page 当前页
     * @param size 每页显示数量
     * @param courseListRequest  扩展功能
     * @return
     */
    QueryResponseResult findCourseList(String company_id,int page, int size, CourseListRequest courseListRequest);

    /**
     * 新增课程
     * @param courseBase
     * @return
     */
    AddCourseResult addCourseBase(CourseBase courseBase);

    /**
     * 根据课程id 查询课程信息
     * @param courseid  课程id
     * @return
     */
    CourseBase getCoursebaseById(String courseid);

    /**
     * 添加课程信息
     * @param id
     * @param courseBase
     * @return
     */
    ResponseResult updateCoursebase(String id, CourseBase courseBase);

    /**
     * 根据课程id 查询课程营销信息
     * @param courseid
     * @return
     */
    CourseMarket getCourseMarketById(String courseid);

    /**
     * 更新课程营销信息
     * @param id
     * @param courseMarket
     * @return
     */
    CourseMarket updateCourseMarket(String id, CourseMarket courseMarket);

    /**
     * 添加课程图片与课程的关联关系
     * @param courseId
     * @param pic
     * @return
     */
    ResponseResult addCoursepic(String courseId, String pic);

    /**
     * 查询课程图片进行数据回显
     * @param courseid
     * @return
     */
    CoursePic findCoursePic(String courseid);

    /**
     * 删除图片
     * @param courseId 图片id
     * @return
     */
    ResponseResult deleteCoursePic(String courseId);

    /**
     * 根据id查询课程视图
     * @param id
     * @return
     */
    CourseView getCourseView(String id);

    /**
     * 课程预览
     * @param id
     * @return
     */
    CoursePublishResult preview(String id);

    /**
     * 课程发布
     * @param id
     * @return
     */
    CoursePublishResult publih(String id);

    /**
     * 保存课程计划与媒资之间的关联
     * @param teachplanMedia
     * @return
     */
    ResponseResult saveMedia(TeachplanMedia teachplanMedia);
}
