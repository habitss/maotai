package com.keytech.modules.post.record.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.post.take.entity.TakeUpPost;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;

/**
 * @Description: 上下岗记录
 * @Author: jeecg-boot
 * @Date:   2025-06-16
 * @Version: V1.0
 */
public interface ITakeUpPostService extends BaseService<TakeUpPost> {
    Result<String> startPost(TakeUpPost takeUpPost, HttpServletRequest request);

    void endPostBatch(String ids);

    void endPostByTime();
}
