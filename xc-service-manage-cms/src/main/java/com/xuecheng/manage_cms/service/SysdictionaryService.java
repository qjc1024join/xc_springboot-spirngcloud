package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;

public interface SysdictionaryService {
    /**
     * 查询课程词典
     * @param type
     * @return
     */
    SysDictionary findDictionaryByType(String type);
}
