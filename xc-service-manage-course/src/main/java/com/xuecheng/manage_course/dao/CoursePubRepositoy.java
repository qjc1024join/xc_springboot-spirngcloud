package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePub;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 课程信息汇总
 */
public interface CoursePubRepositoy extends JpaRepository<CoursePub,String> {
}
