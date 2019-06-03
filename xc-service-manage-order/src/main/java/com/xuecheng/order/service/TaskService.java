package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;

import java.util.Date;
import java.util.List;

public interface TaskService {

    public List<XcTask> findTaskList(Date updateTime, int n);

    public int getTask(String id,int version);

    public void finishTask(String taskId);
    public void publish(XcTask xcTask,String ex,String routingKey);
}
