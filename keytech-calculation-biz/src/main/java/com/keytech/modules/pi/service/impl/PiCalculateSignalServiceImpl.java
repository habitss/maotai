package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.common.CommonConstants;
import com.keytech.modules.pi.common.PiCommonConstants;
import com.keytech.modules.pi.dto.AlgorithmDataDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiCalculateSignalMapper;
import com.keytech.modules.pi.service.*;
import jakarta.annotation.Resource;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * <p>
 * pi-计算信号
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-14 22:34:22
 */
@Service
public class PiCalculateSignalServiceImpl extends BaseServiceImpl<PiCalculateSignalMapper, PiCalculateSignal> implements IPiCalculateSignalService {

    /**
     * 取值步骤
     */
    @Resource
    private IPiRetrievalStepService iPiRetrievalStepService;
    /**
     * 计算模型
     */
    @Resource
    private IPiComputationModelService iPiComputationModelService;
    /**
     * 数据信号
     */
    @Resource
    private IPiRetrievalSignalService iPiRetrievalSignalService;
    /**
     * 计算模型-监控取数
     */
    @Resource
    private IPiComputationMonitorModelService iPiComputationMonitorModelService;

    /**
     * 报表字段配置
     */
    @Resource
    private IPiCustomizeTableFieldService piCustomizeTableFieldService;

    /**
     * 结果转换模型
     */
    @Resource
    private IPiResultTransformationModelService iPiResultTransformationModelService;

    /**
     * 根据picode集合，查询pi对应的所有计算信号对象
     * @param piCodes pi code集合
     * @return
     */
    @Override
    public List<PiCalculateSignal> queryListByPiCodes(String tableName,List<String> piCodes){
        List<PiCalculateSignal> piCalculateSignalList = new ArrayList<>();
        if(null != piCodes && piCodes.size() > 0 && StrUtil.isNotEmpty(tableName)){
            LambdaQueryWrapper<PiCalculateSignal> queryWrapper = Wrappers.lambdaQuery(PiCalculateSignal.class);
            queryWrapper.eq(PiCalculateSignal::getPiTable,tableName);
            queryWrapper.eq(PiCalculateSignal::getIsEnabled,1);
            queryWrapper.in(PiCalculateSignal::getPiCode,piCodes);
            queryWrapper.orderByAsc(PiCalculateSignal::getCreateTime);
            piCalculateSignalList = list(queryWrapper);
        }

        //查询符合条件的取值信号、步骤范围以及计算模型
        //取值信号集合
        List<PiRetrievalSignal> piRetrievalSignalList = new ArrayList<>();
        //取值步骤集合
        List<PiRetrievalStep> piRetrievalStepList = new ArrayList<>();
        //计算模型集合
        List<PiComputationModel> piComputationModelList = new ArrayList<>();
        //监控型算法集合
        List<PiComputationMonitorModel> piComputationMonitorModelList = new ArrayList<>();
        //结果转换模型集合
        List<PiResultTransformationModel> piResultTransformationModelList = new ArrayList<>();

        //查询所有PI基础数据表的字段配置信息
        List<PiCustomizeTableField> customizeTableFields = new ArrayList<>();
        if(null != piCodes && piCodes.size() > 0 && StrUtil.isNotEmpty(tableName)){
            customizeTableFields = piCustomizeTableFieldService.lambdaQuery()
                    .eq(PiCustomizeTableField::getTableName,tableName)
                    .in(PiCustomizeTableField::getFieldName,piCodes)
                    .list();
        }

        if(null != piCalculateSignalList && piCalculateSignalList.size() > 0){
            List<String> calculateSignalIds = piCalculateSignalList.stream()
                    .map(PiCalculateSignal::getId)
                    .collect(Collectors.toList());
            piRetrievalSignalList = iPiRetrievalSignalService.queryListBycalculateIds(calculateSignalIds);
            piRetrievalStepList = iPiRetrievalStepService.queryListBycalculateIds(calculateSignalIds);
            piComputationModelList = iPiComputationModelService.queryListBycalculateIds(calculateSignalIds);
            piComputationMonitorModelList = iPiComputationMonitorModelService.queryListBycalculateIds(calculateSignalIds);

//            //查询结果转换模型，获取转换模型id集合
//            List<String> computationMonitorModelIdsForSignal = piCalculateSignalList.stream()
//                    .map(PiCalculateSignal::getResultModelId)
//                    .collect(Collectors.toList());

            List<String> mergedIds = piComputationModelList.stream()
                    .map(PiComputationModel::getResultModelId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            // 合并两个集合并去重，同时处理空值情况
//            List<String> mergedIds = Stream.concat(
//                            computationMonitorModelIdsForSignal.stream(),
//                            computationMonitorModelIdsForModel.stream()
//                    )
//                    .filter(Objects::nonNull)
//                    .distinct()
//                    .collect(Collectors.toList());
            piResultTransformationModelList = iPiResultTransformationModelService.queryListByResultModelIds(mergedIds);

        }
        for (PiCalculateSignal piCalculateSignal : piCalculateSignalList) {
            //将取值信号、步骤范围以及计算模型设置到piCalculateSignal中
            List<PiRetrievalSignal> piRetrievalSignalListByCalculateId = piRetrievalSignalList.stream()
                    .filter(entity ->entity.getCalculateId().equals(piCalculateSignal.getId()))
                    .collect(Collectors.toList());
            piCalculateSignal.setPiRetrievalSignalList(piRetrievalSignalListByCalculateId);
            //取值步骤集合
            List<PiRetrievalStep> piRetrievalStepListByCalculateId = piRetrievalStepList.stream()
                    .filter(entity ->entity.getCalculateId().equals(piCalculateSignal.getId()))
                    .collect(Collectors.toList());
            piCalculateSignal.setPiRetrievalStepList(piRetrievalStepListByCalculateId);
            //计算模型集合
            List<PiComputationModel> piComputationModelListByCalculateId = piComputationModelList.stream()
                    .filter(entity ->entity.getCalculateId().equals(piCalculateSignal.getId()))
                    .collect(Collectors.toList());
            //查找模型中的结果转换模型，存在就注入
            for (PiComputationModel piComputationModel : piComputationModelListByCalculateId){
                piComputationModel.setResultTransformationModel(piResultTransformationModelList.stream()
                        .filter(entity ->entity.getId().equals(piComputationModel.getResultModelId()))
                        .findFirst()
                        .orElse(null));
            }
            piCalculateSignal.setPiComputationModelList(piComputationModelListByCalculateId);
            //监控型算法集合
            List<PiComputationMonitorModel> piComputationMonitorModelListByCalculateId = piComputationMonitorModelList.stream()
                    .filter(entity ->entity.getCalculateId().equals(piCalculateSignal.getId()))
                    .collect(Collectors.toList());
            piCalculateSignal.setPiComputationMonitorModelList(piComputationMonitorModelListByCalculateId);

            //设置基础数据表的字段配置信息
            List<PiCustomizeTableField> customizeTableFieldsByPi = customizeTableFields.stream()
                    .filter(e->e.getFieldName().equals(piCalculateSignal.getPiCode()) && e.getTableName().equals(tableName))
                    .collect(Collectors.toList());
            piCalculateSignal.setCustomizeTableFieldList(customizeTableFieldsByPi);
//            //查找唯一约束中的结果转换模型，存在就注入
//            piCalculateSignal.setResultTransformationModel(piResultTransformationModelList.stream()
//                    .filter(entity ->entity.getId().equals(piCalculateSignal.getResultModelId()))
//                    .findFirst()
//                    .orElse(null));
        }
        return piCalculateSignalList;
    }

    /**
     * 获取算法数据
     * @param piCalculateSignal
     * @return
     */
    @Override
    public AlgorithmDataDto getAlgorithmData(PiCalculateSignal piCalculateSignal) {
        AlgorithmDataDto algorithmDataDto = new AlgorithmDataDto();
        String calculateId = piCalculateSignal.getId();
        if(StrUtil.isNotBlank(calculateId)){

            //获取取数信号  [[{id:1,identifier:2},{id:2,identifier:2}],[{id:3,identifier:3}]]
            LambdaQueryWrapper<PiRetrievalSignal> retrievalSignalWrapper = Wrappers.lambdaQuery(PiRetrievalSignal.class);
            retrievalSignalWrapper.eq(PiRetrievalSignal::getCalculateId,calculateId);
            retrievalSignalWrapper.orderByAsc(PiRetrievalSignal::getSort);
            retrievalSignalWrapper.orderByAsc(PiRetrievalSignal::getId);
            List<PiRetrievalSignal> retrievalSignalList = iPiRetrievalSignalService.list(retrievalSignalWrapper);
            // 将相同分组的对象归为一组
            Map<String, List<PiRetrievalSignal>> groupedMap = new HashMap<>();
            for (PiRetrievalSignal object : retrievalSignalList) {
                String identifier = object.getIdentifier();
                if (!groupedMap.containsKey(identifier)) {
                    groupedMap.put(identifier, new ArrayList<>());
                }
                groupedMap.get(identifier).add(object);
            }
            // 将分组后的结果转换为符合要求的格式（列表的列表）
            List<List<PiRetrievalSignal>> finalList = new ArrayList<>(groupedMap.values());
            // 使用ObjectMapper将结果转换为JSON字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String json = null;
            try {
                json = objectMapper.writeValueAsString(finalList);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            //System.out.println(json);
            algorithmDataDto.setRetrievalSignal(json);

            //取值步骤
            LambdaQueryWrapper<PiRetrievalStep> retrievalStepWrapper = Wrappers.lambdaQuery(PiRetrievalStep.class);
            retrievalStepWrapper.eq(PiRetrievalStep::getCalculateId,calculateId);
            List<PiRetrievalStep> retrievalStepList = iPiRetrievalStepService.list(retrievalStepWrapper);
            if(null != retrievalStepList && retrievalStepList.size() > 0){
                for (PiRetrievalStep piRetrievalStep : retrievalStepList) {
                    if(piRetrievalStep.getStepType() == 0){
                        //开始
                        algorithmDataDto.setRetrievalStepStart(piRetrievalStep);
                    }else if(piRetrievalStep.getStepType() == 1){
                        //结束
                        algorithmDataDto.setRetrievalStepEnd(piRetrievalStep);
                    }
                }
            }
            //计算模型
            LambdaQueryWrapper<PiComputationModel> computationModelWrapper = Wrappers.lambdaQuery(PiComputationModel.class);
            computationModelWrapper.eq(PiComputationModel::getCalculateId,calculateId);
            List<PiComputationModel> computationModelList = iPiComputationModelService.list(computationModelWrapper);
            if(null != computationModelList && computationModelList.size() > 0){
                algorithmDataDto.setComputationModel(computationModelList.get(0));
            }
            //计算模型-监控取值
            LambdaQueryWrapper<PiComputationMonitorModel> computationMonitorModelWrapper = Wrappers.lambdaQuery(PiComputationMonitorModel.class);
            computationMonitorModelWrapper.eq(PiComputationMonitorModel::getCalculateId,calculateId);
            List<PiComputationMonitorModel> computationMonitorModelList = iPiComputationMonitorModelService.list(computationMonitorModelWrapper);
            if(null != computationMonitorModelList && computationMonitorModelList.size() > 0){
                algorithmDataDto.setComputationMonitorModelList(computationMonitorModelList);
            }
        }
        return algorithmDataDto;
    }

    /**
     * 批量删除算法
     * @param asList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataBatch(List<String> asList) {
        //当前用户
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //取数信号-删除
        LambdaUpdateWrapper<PiRetrievalSignal> retrievalSignalWrapper = new UpdateWrapper().lambda();
        //查询
        retrievalSignalWrapper.in(PiRetrievalSignal::getCalculateId,asList);
        retrievalSignalWrapper.eq(PiRetrievalSignal::getDelFlag,0);
        //删除
        retrievalSignalWrapper.set(PiRetrievalSignal::getDelFlag, 1);
        retrievalSignalWrapper.set(PiRetrievalSignal::getUpdateTime, new Date());
        retrievalSignalWrapper.set(PiRetrievalSignal::getUpdateBy, user.getUsername());
        iPiRetrievalSignalService.update(retrievalSignalWrapper);

        //取数步骤-删除
        LambdaUpdateWrapper<PiRetrievalStep> retrievalStepWrapper = new UpdateWrapper().lambda();
        retrievalStepWrapper.in(PiRetrievalStep::getCalculateId,asList);
        retrievalStepWrapper.eq(PiRetrievalStep::getDelFlag,0);
        retrievalStepWrapper.set(PiRetrievalStep::getDelFlag, 1);
        retrievalStepWrapper.set(PiRetrievalStep::getUpdateTime, new Date());
        retrievalStepWrapper.set(PiRetrievalStep::getUpdateBy, user.getUsername());
        iPiRetrievalStepService.update(retrievalStepWrapper);

        //计算模型-删除
        LambdaUpdateWrapper<PiComputationModel> computationModelWrapper = new UpdateWrapper().lambda();
        computationModelWrapper.in(PiComputationModel::getCalculateId,asList);
        computationModelWrapper.eq(PiComputationModel::getDelFlag,0);
        computationModelWrapper.set(PiComputationModel::getDelFlag, 1);
        computationModelWrapper.set(PiComputationModel::getUpdateTime, new Date());
        computationModelWrapper.set(PiComputationModel::getUpdateBy, user.getUsername());
        iPiComputationModelService.update(computationModelWrapper);

        //计算模型(监控取值)-删除
        LambdaUpdateWrapper<PiComputationMonitorModel> computationMonitorModelWrapper = new UpdateWrapper().lambda();
        computationMonitorModelWrapper.in(PiComputationMonitorModel::getCalculateId,asList);
        computationMonitorModelWrapper.eq(PiComputationMonitorModel::getDelFlag,0);
        computationMonitorModelWrapper.set(PiComputationMonitorModel::getDelFlag, 1);
        computationMonitorModelWrapper.set(PiComputationMonitorModel::getUpdateTime, new Date());
        computationMonitorModelWrapper.set(PiComputationMonitorModel::getUpdateBy, user.getUsername());
        iPiComputationMonitorModelService.update(computationMonitorModelWrapper);

        //删除算法-删除
        removeByIds(asList);
    }

    /**
     * 添加信息
     * @param pageList
     * @return
     */
    @Override
    public IPage<PiCalculateSignal> addInformation(IPage<PiCalculateSignal> pageList) {
        List<PiCalculateSignal> signalList = pageList.getRecords();
        //计算信号 id集合
        List<String> idList = signalList.stream()
                .map(PiCalculateSignal::getId)
                .collect(Collectors.toList());
        if(idList != null && idList.isEmpty()){
            return pageList;
        }
        //#######################################获取取数信号/计算模型###################################
        //获取取数信号
        LambdaQueryWrapper<PiRetrievalSignal> retrievalWrapper = new LambdaQueryWrapper<>();
        retrievalWrapper.in(PiRetrievalSignal::getCalculateId, idList);
        List<PiRetrievalSignal> retrievalList = iPiRetrievalSignalService.list(retrievalWrapper);

        //计算模型-瞬时取值/范围取值
        LambdaQueryWrapper<PiComputationModel> computationWrapper = new LambdaQueryWrapper<>();
        computationWrapper.in(PiComputationModel::getCalculateId, idList);
        List<PiComputationModel> computationList = iPiComputationModelService.list(computationWrapper);
        //计算模型-监控取值
        LambdaQueryWrapper<PiComputationMonitorModel> monitorWrapper = new LambdaQueryWrapper<>();
        monitorWrapper.in(PiComputationMonitorModel::getCalculateId, idList);
        List<PiComputationMonitorModel> monitorList = iPiComputationMonitorModelService.list(monitorWrapper);

        //取数信号
        Map<String, List<PiRetrievalSignal>> retrievalMap = retrievalList.stream()
                .collect(Collectors.groupingBy(PiRetrievalSignal::getCalculateId));
        //瞬时取值/范围取值
        Map<String, List<PiComputationModel>> computationMap = computationList.stream()
                .collect(Collectors.groupingBy(PiComputationModel::getCalculateId));
        //监控取值
        Map<String, List<PiComputationMonitorModel>> monitorMap = monitorList.stream()
                .collect(Collectors.groupingBy(PiComputationMonitorModel::getCalculateId));

        //#######################################数据合并#######################################
        // 遍历signalList并设置合并后的描述
        signalList.forEach(signal -> {
            String signalId = signal.getId();
            //取值类型
            String stepType = signal.getStepType();
            //获取取数信号
            if (retrievalMap.containsKey(signalId)) {
                // 获取所有匹配的retrieval explanations
                // 第一步：过滤并处理每个 explanation
                List<PiRetrievalSignal> processedSignals = retrievalMap.get(signalId).stream()
                    .filter(retrievalsignal -> {
                        String explanation = retrievalsignal.getExplanation();
                        return explanation != null && !explanation.isEmpty()
                                && PiCommonConstants.OPEN.equals(retrievalsignal.getDisplay())
                                && !(PiCommonConstants.CLOSE).equals(retrievalsignal.getDisplay());
                    })
                    .map(retrievalsignal -> {
                        // 使用正则表达式去掉开头的"且"（带空格或不带空格）
                        String processedExp = retrievalsignal.getExplanation().replaceAll("^\\s*且\\s*", "");

                        PiRetrievalSignal newSignal = new PiRetrievalSignal();
                        newSignal.setIdentifier(retrievalsignal.getIdentifier());
                        newSignal.setExplanation(processedExp);
                        return newSignal;
                    })
                    .collect(Collectors.toList());

                // 第二步：按 identifier 分组并用"且"拼接
                String combinedExplanation = processedSignals.stream()
                        .collect(Collectors.groupingBy(
                                PiRetrievalSignal::getIdentifier,
                                Collectors.mapping(
                                        PiRetrievalSignal::getExplanation,
                                        Collectors.joining(" 且 ")  // 相同 identifier 的内容用"且"连接
                                )
                        ))
                        .values().stream()
                        .collect(Collectors.joining("\n"));  // 不同 identifier 组之间用换行符分隔

                signal.setRetrievalExplanation(combinedExplanation);
            }
            // 获取计算模型
            if("2".equals(stepType)){
                //监控取值
                if (monitorMap.containsKey(signalId)) {
                    // 获取所有匹配的retrieval formulaDescribe
                    String combinedExplanation = monitorMap.get(signalId).stream()
                            .map(PiComputationMonitorModel::getFormulaDescribe)
                            .filter(formulaDescribe -> formulaDescribe != null && !formulaDescribe.isEmpty())
                            .collect(Collectors.joining("\n")); // 用换行符连接
                    signal.setFormulaDescribe(combinedExplanation);
                }
            }else{
                //瞬时取值/范围取值
                if (computationMap.containsKey(signalId)) {
                    // 获取所有匹配的retrieval formulaDescribe
                    String combinedExplanation = computationMap.get(signalId).stream()
                            .map(PiComputationModel::getFormulaDescribe)
                            .filter(formulaDescribe -> formulaDescribe != null && !formulaDescribe.isEmpty())
                            .collect(Collectors.joining("\n")); // 用换行符连接
                    signal.setFormulaDescribe(combinedExplanation);
                }
            }
        });
        return pageList;
    }
    /**
     * 判断字段是否是PI间计算使用
     * @param tableName
     * @param fieldName
     */
    @Override
    public boolean isPiOtherCalculation(String tableName, String fieldName) {
        //算法
        LambdaQueryWrapper<PiCalculateSignal> queryWrapper = new LambdaQueryWrapper<PiCalculateSignal>();
        queryWrapper.eq(PiCalculateSignal::getPiTable, tableName);
        queryWrapper.eq(PiCalculateSignal::getPiCode, fieldName);
        queryWrapper.eq(PiCalculateSignal::getStepType, PiCommonConstants.INSTANT); //步骤类型，0瞬时取数
        queryWrapper.eq(PiCalculateSignal::getDelFlag, CommonConstants.NOT_DELETED);
        List<PiCalculateSignal> calculateSignalList = list(queryWrapper);

        List<String> calculateIdList = calculateSignalList.stream()
                .map(PiCalculateSignal::getId)
                .collect(Collectors.toList());
        // 判断 calculateIdList 是否有PI日报间计算
        if(null != calculateIdList && calculateIdList.size() > 0){
            LambdaQueryWrapper<PiComputationModel> modelQueryWrapper = new LambdaQueryWrapper<PiComputationModel>();
            modelQueryWrapper.eq(PiComputationModel::getValueSource,PiCommonConstants.PI_DAILY); //1-pi日报
            modelQueryWrapper.in(PiComputationModel::getCalculateId,calculateIdList);
            modelQueryWrapper.eq(PiComputationModel::getDelFlag, CommonConstants.NOT_DELETED);
            List<PiComputationModel>  modelList = iPiComputationModelService.list(modelQueryWrapper);
            if(null != modelList && modelList.size() > 0){
                return CommonConstants.TRUE;
            }
        }
        return CommonConstants.FALSE;
    }
}
