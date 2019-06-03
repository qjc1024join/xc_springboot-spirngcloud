package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachplanRepository extends JpaRepository<Teachplan,String> {
    /**
     * 根据课程id  与父节点id 查询
     * @param courseid 课程id
     * @param parentid 父节点id
     * @return
     */
    public List<Teachplan> findByCourseidAndAndParentid(String courseid,String parentid);
}
