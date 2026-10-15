package com.keytech.modules.cop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.cop.entity.CopDevice;
import com.keytech.modules.cop.entity.CopPoint;
import com.keytech.base.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * cop清洗点 服务类
 * </p>
 *
 * @author lijun
 * @since 2025-02-18 04:42:21
 */
public interface ICopPointService extends BaseService<CopPoint> {

    void deleteData(String id);

    Page<CopPoint> listData(CopPoint copPoint, Integer pageNo, Integer pageSize, String orderField, String orderType, String selectFields, HttpServletRequest req);

    List<CopPoint> listData(CopPoint copPoint, HttpServletRequest req);

    CopPoint savePoint(CopPoint copPoint);
    /**
     * 自定义导入
     */
    Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<CopPoint> clazz) throws IOException;
}
