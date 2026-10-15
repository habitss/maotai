package com.keytech.modules.df.mdm.service;

import com.keytech.modules.df.mdm.entity.DfTableFieldInfo;
import com.keytech.base.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * @Description: DF数据库表字段信息
 * @Author: jeecg-boot
 * @Date:   2026-01-14
 * @Version: V1.0
 */
public interface IDfTableFieldInfoService extends BaseService<DfTableFieldInfo> {
    /**
     * 查询DF数据库表字段信息
     * @param dfTableName
     * @return
     */
    List<Map<String, Object>> queryDfFieldList(String dfTableName);

    /**
     * 同步DF数据库表字段信息
     * @param dfTableFieldInfo 参数
     * @param dfFieldList 从df抽取过来的表字段列表
     */
    void syncDfTableField(DfTableFieldInfo dfTableFieldInfo, List<Map<String, Object>> dfFieldList);
}
