package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.pi.entity.PiResultTransformationModelDetail;
import com.keytech.modules.pi.mapper.PiResultTransformationModelDetailMapper;
import com.keytech.modules.pi.service.IPiResultTransformationModelDetailService;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.List;

/**
 * @Description: PI-结果转换模型配置
 * @Author: jeecg-boot
 * @Date:   2026-02-05
 * @Version: V1.0
 */
@Service
public class PiResultTransformationModelDetailServiceImpl extends BaseServiceImpl<PiResultTransformationModelDetailMapper, PiResultTransformationModelDetail> implements IPiResultTransformationModelDetailService {

    @Override
    public void saveOrUpdateData(PiResultTransformationModelDetail piResultTransformationModelDetail) {
        String id = piResultTransformationModelDetail.getId();

        //清理相同id
        if(StrUtil.isNotBlank(id)){
            LambdaQueryWrapper<PiResultTransformationModelDetail> queryWrapper = new LambdaQueryWrapper<>();
            //算法id
            queryWrapper.eq(PiResultTransformationModelDetail::getModelId, id);
            //List<PiResultTransformationModelDetail> list = list(queryWrapper);
            remove(queryWrapper);
        }

        //获取json
        String conditionJson = piResultTransformationModelDetail.getConditionJson();
        //解析json 转换成PiResultTransformationModelDetail对象数组
        if(StrUtil.isNotBlank(conditionJson)){
            List<PiResultTransformationModelDetail> detailList = JSONUtil.toList(conditionJson, PiResultTransformationModelDetail.class);
            for (PiResultTransformationModelDetail resultTransformationModelDetail : detailList) {
                resultTransformationModelDetail.setModelId(piResultTransformationModelDetail.getId());
            }
            if (detailList != null && detailList.size() > 0){
                this.saveOrUpdateBatch(detailList);
            }
        }

    }
}
