package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程管理接口
 */
@Mapper
public interface TeachplanMapper {

    /**
     * 课程计划查询 树形结构三级联动
     */
    public TeachplanNode selectList(String courseId);
}
