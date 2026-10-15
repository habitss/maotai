package com.keytech.modules.pi.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.custom.report.mapper.CustomReportMapper;
import com.keytech.modules.dcp.data.service.ISyncDcpDataService;
import com.keytech.modules.df.mdm.entity.DfMdmDeptMapping;
import com.keytech.modules.df.mdm.service.IDfMdmDeptMappingService;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.pi.dto.PiDfAlgorithmDto;
import com.keytech.modules.pi.dto.TableConditionDto;
import com.keytech.modules.pi.dto.TableFieldValueDto;
import com.keytech.modules.pi.dto.TableSaveDto;
import com.keytech.modules.pi.entity.PiDfAlgorithmBaseinfo;
import com.keytech.modules.pi.entity.PiDfAlgorithmModel;
import com.keytech.modules.pi.entity.PiDfAlgorithmUnique;
import com.keytech.modules.pi.mapper.PiDfAlgorithmBaseinfoMapper;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import com.keytech.modules.pi.service.IPiDfAlgorithmBaseinfoService;
import com.keytech.modules.pi.service.IPiDfAlgorithmModelService;
import com.keytech.modules.pi.service.IPiDfAlgorithmUniqueService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: DF算法-基础信息
 * @Author: jeecg-boot
 * @Date:   2026-01-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class PiDfAlgorithmBaseinfoServiceImpl extends BaseServiceImpl<PiDfAlgorithmBaseinfoMapper, PiDfAlgorithmBaseinfo> implements IPiDfAlgorithmBaseinfoService {
    @Resource
    private IPiDfAlgorithmUniqueService piDfAlgorithmUniqueService;
    @Resource
    private IPiDfAlgorithmModelService piDfAlgorithmModelService;
    @Resource
    private IPiCustomizeTableService iPiCustomizeTableService;
    @Resource
    private ISyncDcpDataService iSyncDcpDataService;
    @Resource
    private IMdmDictService mdmDictService;
    @Resource
    private CustomReportMapper customReportMapper;
    @Resource
    private IDfMdmDeptMappingService dfMdmDeptMappingService;

    @Override
    @Transactional
    public void saveOrUpdateData(PiDfAlgorithmDto piDfAlgorithmDto) {
        //如果修改过数据源表，且基础配置表id不为空，则删除旧配置
        if(piDfAlgorithmDto.getIsChangeDfTable() && StrUtil.isNotBlank(piDfAlgorithmDto.getId())){
            List<String> idList = Arrays.asList(piDfAlgorithmDto.getId().split(","));
            removeConfigData(idList);
        }
        //基础配置id为空则添加id
        if(StrUtil.isBlank(piDfAlgorithmDto.getId())){
            piDfAlgorithmDto.setId(IdUtil.getSnowflakeNextIdStr());
        }
        //唯一约束配置
        List<PiDfAlgorithmUnique> piDfAlgorithmUniqueList = piDfAlgorithmDto.getPiDfAlgorithmUniqueList();
        for (PiDfAlgorithmUnique piDfAlgorithmUnique : piDfAlgorithmUniqueList){
            //如果是新增数据，则清理掉临时id
            if(piDfAlgorithmUnique.getId().startsWith("temp")){
                piDfAlgorithmUnique.setId(null);
            }
            piDfAlgorithmUnique.setDataId(piDfAlgorithmDto.getId());
        }
        //模型配置
        List<PiDfAlgorithmModel> piDfAlgorithmModelList = piDfAlgorithmDto.getPiDfAlgorithmModelList();
        for (PiDfAlgorithmModel piDfAlgorithmModel : piDfAlgorithmModelList){
            //如果是新增数据，则清理掉临时id
            if(piDfAlgorithmModel.getId().startsWith("temp")){
                piDfAlgorithmModel.setId(null);
            }
            piDfAlgorithmModel.setDataId(piDfAlgorithmDto.getId());
        }

        //保存基础配置
        this.saveOrUpdate(piDfAlgorithmDto);
        //保存唯一约束数据
        piDfAlgorithmUniqueService.saveOrUpdateBatch(piDfAlgorithmUniqueList);
        //保存模型数据
        piDfAlgorithmModelService.saveOrUpdateBatch(piDfAlgorithmModelList);
    }

    @Override
    @Transactional
    public void removeData(String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        this.removeByIds(idList);
        removeConfigData(idList);
    }

    @Override
    public void syncDfDataJob() {
        log.info("DF数据同步开始---------------->");
        try {
            //当前时间
            LocalDateTime timeUnique = LocalDateTime.now();
            //上个小时整点时间
            LocalDateTime lastHour = LocalDateTime.now()
                    // 减去 1 小时
                    .minusHours(1)
                    // 分钟设为 0
                    .withMinute(0)
                    // 秒设为 0
                    .withSecond(0)
                    // 纳秒设为 0
                    .withNano(0);
            String sDate = DateUtil.format(lastHour, "yyyy-MM-dd HH:mm:ss");
            String eDate = DateUtil.format(timeUnique, "yyyy-MM-dd HH:mm:ss");
            //获取当前算法是DF算法的字段数据
            List<PiDfAlgorithmBaseinfo> baseinfoList = this.lambdaQuery()
                    .exists("select 1 from mes3_pi_customize_table_field a " +
                            " where a.del_flag = 0 " +
                            " and a.current_algorithm = 'DF' " +
                            " and mes3_pi_df_algorithm_baseinfo.pi_table_name = a.table_name " +
                            " and mes3_pi_df_algorithm_baseinfo.pi_field_name = a.field_name ")
                    .orderByAsc(PiDfAlgorithmBaseinfo::getPiTableName)
                    .orderByAsc(PiDfAlgorithmBaseinfo::getPiFieldName)
                    .list();
            //唯一约束数据
            List<PiDfAlgorithmUnique> uniqueList = piDfAlgorithmUniqueService.lambdaQuery().list();
            Map<String, List<PiDfAlgorithmUnique>> uniqueGroupedBy = uniqueList.stream()
                    .collect(Collectors.groupingBy(
                            PiDfAlgorithmUnique::getDataId
                    ));

            //计算模型数据
            List<PiDfAlgorithmModel> modelList = piDfAlgorithmModelService.lambdaQuery().list();
            Map<String, List<PiDfAlgorithmModel>> modelGroupedBy = modelList.stream()
                    .collect(Collectors.groupingBy(
                            PiDfAlgorithmModel::getDataId
                    ));

            //字典数据
            List<MdmDict> dictList = mdmDictService.list();
            Map<String, String> dictGroupedBy = dictList.stream()
                    .collect(Collectors.toMap(
                            dict -> dict.getGroupCode() + "_" + dict.getItemVal(),
                            MdmDict::getItemKey,
                            (existing, replacement) -> existing
                    ));

            //组织架构映射
            List<DfMdmDeptMapping> deptMappingList = dfMdmDeptMappingService.lambdaQuery()
                    .list();
            //将组织架构映射改为map，key为sysDeptCode,value为对象
            Map<String, DfMdmDeptMapping> deptMappingMap = deptMappingList.stream()
                    .collect(Collectors.toMap(
                            DfMdmDeptMapping::getSysDeptCode,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));

            for (PiDfAlgorithmBaseinfo baseInfo : baseinfoList) {
                String factoryCodes = null;
                //判断是否选择工厂
                if(StrUtil.isNotBlank(baseInfo.getFactoryCode())) {
                    //将工厂按逗号拆分为list
                    List<String> factoryList = Arrays.asList(baseInfo.getFactoryCode().split(","));
                    //循环工厂list，用deptMappingMap匹配后用逗号拼接
                    factoryCodes = factoryList.stream()
                            .map(deptMappingMap::get)
                            .filter(Objects::nonNull)
                            .map(DfMdmDeptMapping::getMdmFullCode)
                            .collect(Collectors.joining("','"));
                    factoryCodes = "'" + factoryCodes + "'";
                }

                //判断是否有实例化指标配置，没有的话就取指标标准数据
                String indicator;
                if(StrUtil.isNotBlank(baseInfo.getIndicator())){
                    indicator = "'" + baseInfo.getIndicator().replace(",", "','") + "'";
                }else{
                    indicator = StrUtil.isBlank(baseInfo.getIndStand()) ? null : "'" + baseInfo.getIndStand() + "'";
                }
                //获取DF数据
                List<Map<String, Object>> dfList = iSyncDcpDataService.queryDcpData(
                        baseInfo.getDataTableName(),
                        baseInfo.getDataFieldName(),
                        baseInfo.getFactoryField(),
                        factoryCodes,
                        sDate,
                        eDate,
                        baseInfo.getIndicatorFieldName(),
                        indicator
                );

                //获取算法唯一约束配置
                List<PiDfAlgorithmUnique> uniqueConfigList = uniqueGroupedBy.get(baseInfo.getId());
                //获取算法模型配置
                List<PiDfAlgorithmModel> modelConfigList = modelGroupedBy.get(baseInfo.getId());
                for(Map<String, Object> innerMap : dfList){
                    //获取约束条件
                    List<TableConditionDto> conditions = new ArrayList<>();
                    //获取字段
                    List<TableFieldValueDto> fieldList = new ArrayList<>();
                    //循环算法唯一约束配置
                    for(PiDfAlgorithmUnique unique : uniqueConfigList){
                        String key = unique.getPiFieldName();
                        Object value = null;
                        //组装唯一约束的值
                        if(StrUtil.isNotBlank(unique.getDateType())){
                            //自定义时间
                            //4位年
                            if(unique.getDateType().equals("yearFun")){
                                value = timeUnique.getYear()+"";
                            }
                            //2位年
                            if(unique.getDateType().equals("year2Fun")){
                                String year = String.valueOf(timeUnique.getYear());
                                year = year.substring(year.length()-2);
                                value = year;
                            }
                            //获取当前小时
                            if(unique.getDateType().equals("hourFun")){
                                // 创建一个日期时间格式化器，用于自定义输出格式
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
                                // 格式化当前的日期和时间
                                value = timeUnique.format(formatter) + ":00:00";
                            }
                            //获取当前小时（偶数）
                            if(unique.getDateType().equals("hour2Fun")){
                                int hour = timeUnique.getHour();
                                //判断小数是否是偶数
                                if(hour % 2 != 0){
                                    timeUnique = timeUnique.minusHours(1);
                                }
                                // 创建一个日期时间格式化器，用于自定义输出格式
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
                                // 格式化当前的日期和时间
                                value = timeUnique.format(formatter) + ":00:00";
                            }
                        } else if (StrUtil.isNotBlank(unique.getInputText())){
                            //自定义内容
                            value = unique.getInputText();
                        } else {
                            //获取DF数据
                            value = innerMap.get(unique.getDfFieldName());
                        }
                        String valueStr = null != value ? value.toString() : null;
                        //添加筛选条件
                        TableConditionDto dto = new TableConditionDto();
                        dto.setKey(key);
                        dto.setVal(valueStr);
                        conditions.add(dto);

                        //添加字段值
                        TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
                        tableFieldValueDto.setField(key);
                        tableFieldValueDto.setFieldVal(valueStr);
                        fieldList.add(tableFieldValueDto);
                    }

                    for(PiDfAlgorithmModel calcModel : modelConfigList){
                        if("0".equals(calcModel.getIfCover())){
                            continue;
                        }
                        String key = calcModel.getPiFieldName();
                        String value = "";
                        if("99".equals(calcModel.getIfSinicized())){
                            //自定义内容
                            value = calcModel.getSinicizedDic() ;
                        }else if(null != innerMap.get(calcModel.getDfFieldName())){
                            value = innerMap.get(calcModel.getDfFieldName()).toString();
                            //结果日期格式化
                            if(null != calcModel.getResultType()){
                                value = switch (calcModel.getResultType()) {
                                    case "1" ->
                                        //天
                                            DateUtil.format(DateUtil.parse(value), "yyyy-MM-dd");
                                    case "2" ->
                                        //时
                                            DateUtil.format(DateUtil.parse(value), "yyyy-MM-dd HH");
                                    case "3" ->
                                        //分
                                            DateUtil.format(DateUtil.parse(value), "yyyy-MM-dd HH:mm");
                                    case "4" ->
                                        //秒
                                            DateUtil.format(DateUtil.parse(value), "yyyy-MM-dd HH:mm:ss");
                                    default -> value;
                                };
                            }
                            //汉化方式
                            if("1".equals(calcModel.getIfSinicized())){
                                String dicKey = calcModel.getSinicizedDic() + "_" + value;
                                value = dictGroupedBy.get(dicKey);
                            }else if("2".equals(calcModel.getIfSinicized())){
                                List<Map<String, Object>> tempList = customReportMapper.getTableData(
                                        "\""+ calcModel.getSinicizedValue()+"\"",
                                        "\""+ calcModel.getSinicizedDic()+"\"",
                                        "\""+calcModel.getSinicizedKey()+"\" = '"+value+"'"
                                );
                                if(CollectionUtil.isNotEmpty(tempList)){
                                    value = tempList.get(0).get(calcModel.getSinicizedValue()).toString();
                                }
                            }
                        }

                        TableFieldValueDto dto = new TableFieldValueDto();
                        dto.setField(key);
                        dto.setFieldVal(value);
                        fieldList.add(dto);
                    }

                    // 写表对象
                    TableSaveDto tableSaveDto = new TableSaveDto();
                    tableSaveDto.setConditionList(conditions);
                    tableSaveDto.setFieldList(fieldList);
                    tableSaveDto.setTable(baseInfo.getPiTableName());
                    iPiCustomizeTableService.tableSave(tableSaveDto);
                }
            }
            log.info("DF数据同步结束---------------->");
        } catch (Exception e) {
            log.error("DF数据同步异常---------------->{}", e.getMessage());
        }
    }

    /**
     * 根据基础数据删除配置
     * @param idList 基础数据id
     */
    private void removeConfigData(List<String> idList){
        //删除唯一约束配置
        piDfAlgorithmUniqueService.lambdaUpdate()
                .in(PiDfAlgorithmUnique::getDataId, idList)
                .remove();
        //删除计算模型配置
        piDfAlgorithmModelService.lambdaUpdate()
                .in(PiDfAlgorithmModel::getDataId, idList)
                .remove();
    }
}
