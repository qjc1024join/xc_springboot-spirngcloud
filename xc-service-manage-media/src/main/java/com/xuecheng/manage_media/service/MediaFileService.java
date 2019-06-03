package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;

public interface MediaFileService {
    /**
     * 查询媒资列表
     * @param page 当前页
     * @param size 每页显示数量
     * @param queryMediaFileRequest 查询条件
     * @return
     */
    QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest);

}
