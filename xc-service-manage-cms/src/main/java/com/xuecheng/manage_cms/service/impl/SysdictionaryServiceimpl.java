package com.xuecheng.manage_cms.service.impl;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.SysDictionaryDao;
import com.xuecheng.manage_cms.service.SysdictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysdictionaryServiceimpl implements SysdictionaryService {

   @Autowired
   private SysDictionaryDao sysDictionarydao;

    @Override
    public SysDictionary findDictionaryByType(String type) {
        return sysDictionarydao.findBydType(type);
    }
}
