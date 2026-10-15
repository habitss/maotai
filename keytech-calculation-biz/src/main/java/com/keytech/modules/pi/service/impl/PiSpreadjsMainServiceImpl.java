package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.pi.entity.PiBasePoint;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.keytech.modules.pi.entity.PiSpreadjsMain;
import com.keytech.modules.pi.entity.PiSpreadjsPi;
import com.keytech.modules.pi.mapper.PiSpreadjsMainMapper;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import com.keytech.modules.pi.service.IPiSpreadjsMainService;
import com.keytech.modules.pi.service.IPiSpreadjsPiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.List;

/**
 * @Description: SpreadJS模板配置
 * @Author: jeecg-boot
 * @Date:   2025-12-23
 * @Version: V1.0
 */
@Service
public class PiSpreadjsMainServiceImpl extends BaseServiceImpl<PiSpreadjsMainMapper, PiSpreadjsMain> implements IPiSpreadjsMainService {

    @Autowired
    private IPiSpreadjsPiService piSpreadjsPiService;
    @Autowired
    private IPiCustomizeTableService iPiCustomizeTableService;

    /**
     * 保存PI配置
     * @param piSpreadjsPi
     */
    @Override
    public void savePiConfig(PiSpreadjsPi piSpreadjsPi) {
        PiCustomizeTable piCustomizeTable = null;
        if(StrUtil.isBlank(piSpreadjsPi.getTableName())) {
            piCustomizeTable = iPiCustomizeTableService.getById(piSpreadjsPi.getTableId());
            if (piCustomizeTable != null) {
                piSpreadjsPi.setTableName(piCustomizeTable.getTableName());
            }
        }
        piSpreadjsPiService.saveOrUpdate(piSpreadjsPi);
    }

    /**
     * 获取PI配置
     */
    @Override
    public List<PiSpreadjsPi> getPiConfig(String spreadjsMainId) {
        List<PiSpreadjsPi> list = piSpreadjsPiService.list(new LambdaQueryWrapper<PiSpreadjsPi>()
                        .eq(PiSpreadjsPi::getSpreadjsMainId, spreadjsMainId));
        return list;
    }
}
