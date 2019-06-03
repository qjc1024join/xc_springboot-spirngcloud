package com.xuecheng.framework.domain.course.ext;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@NoArgsConstructor
@ToString
public class CourseView implements Serializable {
    /*课程信息*/
    private CourseBase courseBase;
    /*课程图片*/
    private CoursePic coursePic;
    /*课程营销信息*/
    private CourseMarket courseMarket;
    /*教学计划树形结构*/
    private TeachplanNode teachplanNode;
}
