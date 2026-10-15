package com.keytech.modules.cop.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.cop.entity.CopConfig;
import com.keytech.modules.cop.entity.CopPoint;
import com.keytech.modules.cop.mapper.CopConfigMapper;
import com.keytech.modules.cop.service.ICopConfigService;
import com.keytech.modules.cop.service.ICopPointService;
import com.keytech.modules.mdm3.entity.MdmItem;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * cop清洗项目配置 服务实现类
 * </p>
 *
 * @author lijun
 * @since 2025-02-19 02:03:56
 */
@Service
public class CopConfigServiceImpl extends BaseServiceImpl<CopConfigMapper, CopConfig> implements ICopConfigService {
    @Resource
    private ICopPointService copPointService;

    @Override
    @Transactional
    public void saveOrUpdateData(CopConfig copConfig, Boolean isAdd) {
        List<CopConfig> list = this.lambdaQuery().eq(CopConfig::getCopPointId, copConfig.getCopPointId())
                .eq(CopConfig::getFieldName, copConfig.getFieldName())
                .ne(isAdd == false, CopConfig::getId, copConfig.getId())
                .list();
        if(CollUtil.isNotEmpty(list)){
            throw new JeecgBootException("该字段已存在");
        }
        if(0 == copConfig.getIsSync()){
            //不需要同步
            //直接保存
            this.saveOrUpdate(copConfig);
        } else {
            //需要同步
            String fieldName = copConfig.getFieldName();
            if(!isAdd){
                //获取该配置的旧数据
                CopConfig oldConfig = this.lambdaQuery().eq(CopConfig::getId, copConfig.getId()).one();
                fieldName = oldConfig.getFieldName();
            }
            //获取该设备下其它清洗点
            List<CopPoint> pointList = copPointService.lambdaQuery()
                    .eq(CopPoint::getCopDeviceId,copConfig.getCopDeviceId())
                    .ne(CopPoint::getId,copConfig.getCopPointId())
                    .list();
            //获取所有清洗点同名配置
            List<CopConfig> configList = this.lambdaQuery()
                    .in(CopConfig::getCopPointId, pointList.stream().map(CopPoint::getId).toList())
                    .eq(CopConfig::getFieldName, fieldName)
                    .list();
            //按清洗点转为map
            Map<String, CopConfig> configMap = configList.stream()
                    .collect(Collectors.toMap(
                            CopConfig::getCopPointId,
                            item -> item,
                            // 如果有重复 key，保留第一个
                            (existing, replacement) -> existing
                    ));
            List<CopConfig> newConfigList = new ArrayList<>();
            newConfigList.add(copConfig);
            //循环清洗点处理配置数据
            for(CopPoint point : pointList){
                CopConfig oldConfig = configMap.get(point.getId());
                if(null == oldConfig){
                    oldConfig = new CopConfig();
                    BeanUtil.copyProperties(copConfig, oldConfig, "id");
                    oldConfig.setCopPointId(point.getId());
                }
                oldConfig.setFieldName(copConfig.getFieldName());
                oldConfig.setFieldType(copConfig.getFieldType());
                newConfigList.add(oldConfig);
            }
            this.saveOrUpdateBatch(newConfigList);
        }
    }

    @Override
    @Transactional
    public void deleteData(CopConfig copConfig) {
        if(0 == copConfig.getIsSync()){
            this.removeById(copConfig.getId());
        } else {
            //获取该设备下其它清洗点
            List<CopPoint> pointList = copPointService.lambdaQuery()
                    .eq(CopPoint::getCopDeviceId,copConfig.getCopDeviceId())
                    .list();
            this.lambdaUpdate()
                    .in(CopConfig::getCopPointId, pointList.stream().map(CopPoint::getId).toList())
                    .eq(CopConfig::getFieldName, copConfig.getFieldName())
                    .remove();
        }
    }
}
