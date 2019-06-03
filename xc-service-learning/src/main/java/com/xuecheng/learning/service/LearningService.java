package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.learning.GetMediaResult;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;

import java.util.Date;

public interface LearningService {
    /**
     * 获取视屏的课程地址
     * @param courseId
     * @param teachplanId
     * @return
     */
    GetMediaResult getmedia(String courseId, String teachplanId);

    /**
     * 添加课程
     * @param userId
     * @param courseId
     * @param valid
     * @param startTime
     * @param endTime
     * @param xcTask
     * @return
     */
    ResponseResult addcourse(String userId, String courseId, String valid, Date startTime, Date endTime, XcTask xcTask);

}
