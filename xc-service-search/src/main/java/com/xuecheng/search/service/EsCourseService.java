package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;

import java.util.Map;

public interface EsCourseService {

    QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam);

    /**
     * 根据id查询课程信息
     * @param courseId
     * @return
     */
    Map<String, CoursePub> getall(String courseId);

    /**
     * 根据课程计划id 查询媒资信息
     * @param teachplanIds
     * @return
     */
    QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds);
}
