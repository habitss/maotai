package com.keytech.modules.cop.service;


import com.keytech.base.service.BaseService;
import com.keytech.modules.cop.entity.CopConfig;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 * cop清洗项目配置 服务类
 * </p>
 *
 * @author lijun
 * @since 2025-02-19 02:03:56
 */
public interface ICopConfigService extends BaseService<CopConfig> {
    void saveOrUpdateData(CopConfig copConfig, Boolean isAdd);
    void deleteData(CopConfig copConfig);
}
