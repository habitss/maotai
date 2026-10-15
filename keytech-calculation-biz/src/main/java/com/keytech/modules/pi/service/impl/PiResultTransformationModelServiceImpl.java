package com.keytech.modules.pi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.pi.entity.PiComputationMonitorModel;
import com.keytech.modules.pi.entity.PiResultTransformationModel;
import com.keytech.modules.pi.entity.PiResultTransformationModelDetail;
import com.keytech.modules.pi.entity.PiRetrievalSignal;
import com.keytech.modules.pi.mapper.PiResultTransformationModelMapper;
import com.keytech.modules.pi.service.IPiAdapterUnitService;
import com.keytech.modules.pi.service.IPiResultTransformationModelDetailService;
import com.keytech.modules.pi.service.IPiResultTransformationModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: PI-结果转换模型配置-主表
 * @Author: jeecg-boot
 * @Date:   2026-02-05
 * @Version: V1.0
 */
@Service
public class PiResultTransformationModelServiceImpl extends BaseServiceImpl<PiResultTransformationModelMapper, PiResultTransformationModel> implements IPiResultTransformationModelService {

    @Autowired
    private IPiResultTransformationModelDetailService iPiResultTransformationModelDetailService;


    /**
     * 通过id的集合，查询对应的主表信息和明细表信息
     * @param resultModelIds
     * @return
     */
    @Override
    public List<PiResultTransformationModel> queryListByResultModelIds(List<String> resultModelIds){
        //主数据
        List<PiResultTransformationModel> piResultTransformationModelList = new ArrayList<>();

        //明细数据
        List<PiResultTransformationModelDetail> piResultTransformationModelDetailList = new ArrayList<>();

        if(null != resultModelIds && resultModelIds.size() > 0){
            LambdaQueryWrapper<PiResultTransformationModel> queryWrapper = Wrappers.lambdaQuery(PiResultTransformationModel.class);
            queryWrapper.in(PiResultTransformationModel::getId,resultModelIds);
            piResultTransformationModelList = list(queryWrapper);

            //查询明细
            LambdaQueryWrapper<PiResultTransformationModelDetail> queryWrapperDetail = Wrappers.lambdaQuery(PiResultTransformationModelDetail.class);
            queryWrapperDetail.in(PiResultTransformationModelDetail::getModelId,resultModelIds);
            queryWrapperDetail.orderByAsc(PiResultTransformationModelDetail::getSort);
            piResultTransformationModelDetailList = iPiResultTransformationModelDetailService.list(queryWrapperDetail);
        }
        //将明细数据注入的主数据中
        if (null != piResultTransformationModelList && piResultTransformationModelList.size() > 0){
            for (PiResultTransformationModel piResultTransformationModel : piResultTransformationModelList) {
                List<PiResultTransformationModelDetail> piResultTransformationModelDetailListTemp =
                        piResultTransformationModelDetailList
                                .stream().filter(detail -> detail.getModelId().equals(piResultTransformationModel.getId()))
                                .toList();
                if (null != piResultTransformationModelDetailListTemp && piResultTransformationModelDetailListTemp.size() > 0){
                    piResultTransformationModel.setPiResultTransformationModelDetailList(piResultTransformationModelDetailListTemp);
                }
            }
        }

        return piResultTransformationModelList;
    }

    @Override
    public void delete(List<String> asList) {
        //删除明细表
        iPiResultTransformationModelDetailService.remove(
                Wrappers.lambdaQuery(PiResultTransformationModelDetail.class)
                .in(PiResultTransformationModelDetail::getModelId,asList));
        //删除主表
        removeByIds(asList);
    }

    @Override
    public void saveOrUpdateData(PiResultTransformationModel piResultTransformationModel) {

        // 检查名称是否已存在（排除自身）
        LambdaQueryWrapper<PiResultTransformationModel> queryWrapper = Wrappers.lambdaQuery(PiResultTransformationModel.class);
        queryWrapper.eq(PiResultTransformationModel::getModelName, piResultTransformationModel.getModelName())
                .eq(PiResultTransformationModel::getDelFlag, 0); // 只检查未删除的

        if (piResultTransformationModel.getId() != null) {
            queryWrapper.ne(PiResultTransformationModel::getId, piResultTransformationModel.getId());
        }
        long count = count(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("模型名称【" + piResultTransformationModel.getModelName() + "】已存在，请使用其他名称");
        }
        saveOrUpdate(piResultTransformationModel);
    }
}
