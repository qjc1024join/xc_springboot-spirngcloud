package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePic;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 课程图片
 */
public interface CoursePicRepository extends JpaRepository<CoursePic,String> {

    long deleteByCourseid(String courseId);
}
