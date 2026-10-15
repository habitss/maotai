package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiPost;
import com.keytech.base.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * &#064;Description:  PI岗位绑定
 * &#064;Author: jeecg-boot
 * &#064;Date:   2025-07-14
 * &#064;Version: V1.0
 */
public interface IPiPostService extends BaseService<PiPost> {
    /**
     * 获取pi填报分页数据
     * @param piPost 绑定数据
     * @param pageNo 当前页
     * @param pageSize 每页大小
     * @param tableName 表名
     * @param req 请求参数
     * @return
     */
    Map<String, Object> queryPageAppList(PiPost piPost, Integer pageNo, Integer pageSize, String tableName, HttpServletRequest req);
}
