package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachplanMediaRepository extends JpaRepository<TeachplanMedia,String> {
    /**
     * 根据课程id 查询
     * @param id 课程id
     * @return
     */
    List<TeachplanMedia> findByCourseId(String id);


}
