package com.xuecheng.filesystem.dao;

import com.xuecheng.framework.domain.filesystem.FileSystem;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 使用mongodb  存储数据
 */
public interface FileSystemRepository extends MongoRepository<FileSystem,String> {
}
