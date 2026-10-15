package com.keytech.modules.df.mdm.mapper;

import com.keytech.modules.df.mdm.entity.DfMdmIndicator;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * MDM-指标主数据表
 * @author yepeng
 * @since 2025.12.08
 * */
@Mapper
public interface DfMdmIndicatorMapper extends BaseMapper<DfMdmIndicator> {
    /**
     * 批量插入或更新
     * @param list 列表
     * @since 2025.12.08
     */
    void batchInsertOrUpdate(@Param("list") List<DfMdmIndicator> list);

    /**
     * 查询设备类型
     * @return 设备类型集合
     */
    List<DfMdmIndicator> queryDeviceTypeGroup();
    /**
     * 查询设备
     * @return 设备信息集合
     */
    List<DfMdmIndicator> queryDeviceGroup();
}
