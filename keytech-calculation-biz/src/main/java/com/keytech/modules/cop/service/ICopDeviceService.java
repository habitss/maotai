package com.keytech.modules.cop.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.cop.entity.CopDevice;
import com.keytech.modules.period.entity.PeriodVerificationDevice;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * cop设备管理 服务类
 * </p>
 *
 * @author lijun
 * @since 2025-02-18 02:08:08
 */
public interface ICopDeviceService extends BaseService<CopDevice> {

    /**
     * COP设备管理-通过id删除
     * @param id
     */
    void deleteData(String id);

    /**
     * COP设备管理-通过id删除
     * @param ids
     */
    void removeByIds(List<String> ids);

    /**
     * 翻页查询
     * @param copDevice
     * @param pageNo
     * @param pageSize
     * @param orderField
     * @param orderType
     * @param req
     * @return
     */
    Page<CopDevice> listData(CopDevice copDevice, Integer pageNo, Integer pageSize, String orderField, String orderType, String selectFields, HttpServletRequest req);

    /**
     * 自定义导入
     */
    Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<CopDevice> clazz) throws IOException;
}
