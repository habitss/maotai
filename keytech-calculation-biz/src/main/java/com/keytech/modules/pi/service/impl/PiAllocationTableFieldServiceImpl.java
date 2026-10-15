package com.keytech.modules.pi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.common.DataBaseTypeUtil;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.fivewhy.service.IFiveWhyAnalyseService;
import com.keytech.modules.pi.common.AllocationTable;
import com.keytech.modules.pi.common.CommonConstants;
import com.keytech.modules.pi.common.PiCommonConstants;
import com.keytech.modules.pi.dto.*;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.service.IPiApproveService;
import com.keytech.modules.pi.mapper.PiAllocationTableFieldMapper;
import com.keytech.modules.pi.service.*;
import com.keytech.modules.pi.util.ConditionParser;
import com.keytech.modules.pi.util.SqlInjectionUtil;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * @Description: 报表字段分配
 * @Author: jeecg-boot
 * @Date:   2024-11-27
 * @Version: V1.0
 */
@Service
public class PiAllocationTableFieldServiceImpl extends BaseServiceImpl<PiAllocationTableFieldMapper, PiAllocationTableField> implements IPiAllocationTableFieldService {

    private static final Double DEFAULT_SORT_VALUE = 9999.0;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    @Resource
    private IPiCustomizeTableFieldService iPiCustomizeTableFieldService;
    @Resource
    private IPiCustomizeTableService iPiCustomizeTableService;

    @Resource
    private PiAllocationTableFieldMapper piAllocationTableFieldMapper;
    @Resource
    private IPiAllocationTableService iPiAllocationTableService;

    @Resource
    private IPiStandardDataService iPiStandardDataService;

    @Resource
    private IPiStandardMaintenanceService iPiStandardMaintenanceService;

    @Autowired
    private IPiAllocationTableFieldService iPiAllocationTableFieldService;

    //5w
    @Autowired
    private IFiveWhyAnalyseService fiveWhyAnalyseService;
    @Resource
    private IPiCalculateSignalService iPiCalculateSignalService;
    @Resource
    private IPiAdapterAlgorithmService iPiAdapterAlgorithmService;
    @Resource
    private IPiComputationModelService iPiComputationModelService;
    @Autowired
    private IPiAllocationTableConditionService iPiAllocationTableConditionService;
    
    @Autowired
    private IPiApproveService piApproveService;

    @PreDestroy
    public void shutdownExecutorService() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(600, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }


    /**
     * 获取分配/未分配字段
     * @param piAllocationTable
     * @return
     */
    @Override
    public List<TransferDataDto> getAllocationField(PiAllocationTable piAllocationTable) {
        List<TransferDataDto> list = new ArrayList<>();
        if(StrUtil.isNotBlank(piAllocationTable.getTableId()) && StrUtil.isNotBlank(piAllocationTable.getId())){
            //获取当前分配表已经分配的字段
            LambdaQueryWrapper<PiAllocationTableField> queryWrapper = new LambdaQueryWrapper<>();
            //自定义报表id
            //queryWrapper.eq(PiAllocationTableField::getTableId,piAllocationTable.getTableId());
            //是否列表显示
            queryWrapper.eq(piAllocationTable.getIsList() != null, PiAllocationTableField::getIsList,piAllocationTable.getIsList());
            //是否多表
            if(AllocationTable.MULTIPLETABLES_YES.equals(piAllocationTable.getMultipleTables())){
                queryWrapper.in(PiAllocationTableField::getTableId,piAllocationTable.getTableId().split(","));
            }else{
                queryWrapper.eq(PiAllocationTableField::getTableId,piAllocationTable.getTableId());
            }

            //分配主表id
            queryWrapper.eq(PiAllocationTableField::getAllocationTableId,piAllocationTable.getId());
            List<PiAllocationTableField> fieldList = list(queryWrapper);
            Map<String, PiAllocationTableField> AllocationTableFieldMap = fieldList.stream()
                    .collect(Collectors.toMap(PiAllocationTableField::getFieldId, p -> p));

            //获取字段表数据
            LambdaQueryWrapper<PiCustomizeTableField> customizeFieldQueryWrapper = new LambdaQueryWrapper<>();
//            customizeFieldQueryWrapper.eq(PiCustomizeTableField::getTableId,piAllocationTable.getTableId());
            if(AllocationTable.MULTIPLETABLES_YES.equals(piAllocationTable.getMultipleTables())){
                customizeFieldQueryWrapper.in(PiCustomizeTableField::getTableId,piAllocationTable.getTableId().split(","));
            }else{
                customizeFieldQueryWrapper.eq(PiCustomizeTableField::getTableId,piAllocationTable.getTableId());
            }
            //排序
            if(AllocationTable.MULTIPLETABLES_YES.equals(piAllocationTable.getMultipleTables())){
                customizeFieldQueryWrapper.orderByAsc(PiCustomizeTableField::getTableName).orderByAsc(PiCustomizeTableField::getSort);
            }else{
                customizeFieldQueryWrapper.orderByAsc(PiCustomizeTableField::getSort);
            }
            List<PiCustomizeTableField> customizeFieldList = iPiCustomizeTableFieldService.list(customizeFieldQueryWrapper);

            //数据融合
            for (PiCustomizeTableField piCustomizeTableField : customizeFieldList) {
                TransferDataDto transferDataDto = new TransferDataDto();
                transferDataDto.setKey(piCustomizeTableField.getId());
                if(AllocationTable.MULTIPLETABLES_YES.equals(piAllocationTable.getMultipleTables())){
                    transferDataDto.setTitle("["+piCustomizeTableField.getTableName()+"]"+piCustomizeTableField.getName()
                            +"("+piCustomizeTableField.getFieldName()+")");
                }else{
                    transferDataDto.setTitle(piCustomizeTableField.getName()+"("+piCustomizeTableField.getFieldName()+")");
                }
                transferDataDto.setDescription(piCustomizeTableField.getName());
                transferDataDto.setField(piCustomizeTableField.getFieldName());
                transferDataDto.setSort(piCustomizeTableField.getSort());
                transferDataDto.setTableName(piCustomizeTableField.getTableName());
                transferDataDto.setDisabled(false);
                if (AllocationTableFieldMap.containsKey(piCustomizeTableField.getId())){
                    transferDataDto.setChosen(true);
                }else{
                    transferDataDto.setChosen(false);
                }
                list.add(transferDataDto);
            }
        }
        return list;
    }


    /**
     * 保存分配的字段
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fieldSave(PiAllocationTableField piAllocationTableField) {
        //自定义表id
        String tableId = piAllocationTableField.getTableId();
        //分配表主表id
        String allocationTableId = piAllocationTableField.getAllocationTableId();
        //分配字段集合
        String fields = piAllocationTableField.getFields();
        if(StrUtil.isNotBlank(fields) && StrUtil.isNotBlank(tableId) && StrUtil.isNotBlank(allocationTableId)){
            // 字段id集合
            String[] field = fields.split(",");
            // 新的字段集合
            List<String> fieldNewList = Arrays.stream(field).toList();

            //获取之前存在的分配的字段
            LambdaQueryWrapper<PiAllocationTableField> queryWrapper = new LambdaQueryWrapper<>();
            //自定义报表id
            //queryWrapper.eq(PiAllocationTableField::getTableId,tableId);
            //分配主表id
            queryWrapper.eq(PiAllocationTableField::getAllocationTableId,allocationTableId);
            List<PiAllocationTableField> fieldList = list(queryWrapper);
            //之前的字段id集合
            List<String> fieldOldList = fieldList.stream().map(PiAllocationTableField::getFieldId).collect(Collectors.toList());

            //匹配新增/删除
            //新增
            List<String> addList = new ArrayList<>(fieldNewList);
            addList.removeAll(fieldOldList);
            //删除
            List<String> delList = new ArrayList<>(fieldOldList);
            delList.removeAll(fieldNewList);

            //新增
            List<PiCustomizeTableField> tableFieldList = new ArrayList<>();
            if(null != addList && addList.size() > 0){
                //获取自定义字段
                List<PiAllocationTableField> listAdd = new ArrayList<>();
                LambdaQueryWrapper<PiCustomizeTableField> tableFieldqueryWrapper = new LambdaQueryWrapper<>();
                //tableFieldqueryWrapper.eq(PiCustomizeTableField::getTableId,tableId);
                tableFieldqueryWrapper.in(PiCustomizeTableField::getId,addList);
                tableFieldList = iPiCustomizeTableFieldService.list(tableFieldqueryWrapper);
                for (PiCustomizeTableField piCustomizeTableField : tableFieldList) {
                    PiAllocationTableField piAllocationTableFieldNew = BeanUtil.copyProperties(piCustomizeTableField,PiAllocationTableField.class,"createTime","createBy","updateTime","updateBy");
                    piAllocationTableFieldNew.setFieldId(piCustomizeTableField.getId());
                    piAllocationTableFieldNew.setAllocationTableId(allocationTableId);
                    piAllocationTableFieldNew.setId(null);
                    if(AllocationTable.MULTIPLETABLES_YES.equals(piAllocationTableField.getMultipleTables())){
                        //多表字段
                        piAllocationTableFieldNew.setFieldName(piCustomizeTableField.getTableName()+"_f_"+piCustomizeTableField.getFieldName());
                    }
                    listAdd.add(piAllocationTableFieldNew);
                }
                if(null != listAdd && listAdd.size() > 0){
                    saveBatch(listAdd);
                }
            }

            //删除
            if(null != delList && delList.size() > 0){
                //在fieldList(之前的字段集合)中找字段id(fieldId) 相同的
                List<PiAllocationTableField> dels = fieldList.stream()
                        .filter(obj -> delList.contains(obj.getFieldId()))
                        .collect(Collectors.toList());
                removeByIds(dels);
            }

            // 判断是否多表
            if(AllocationTable.MULTIPLETABLES_YES.equals(piAllocationTableField.getMultipleTables())){
                //关联方式
                String correlation = piAllocationTableField.getCorrelation();
                //更新序号集合
                queryWrapper = new LambdaQueryWrapper<>();
                //分配主表id
                queryWrapper.eq(PiAllocationTableField::getAllocationTableId,allocationTableId);
                fieldList = list(queryWrapper);
                //设置序号,按配置顺序设置
                //创建映射
                Map<String, Double> idToSortMap = new HashMap<>();
                for (int i = 0; i < fieldNewList.size(); i++) {
                    idToSortMap.put(fieldNewList.get(i), (double) (i + 1)); // id -> 序号映射，转换为Double
                }
                // 遍历设置序号
                for (PiAllocationTableField allocationTableField : fieldList) {;
                    Double sort = Optional.ofNullable(idToSortMap.get(allocationTableField.getFieldId()))
                            .orElse(DEFAULT_SORT_VALUE);
                    allocationTableField.setSort(sort);
                }
                if (!fieldList.isEmpty()) {
                    iPiAllocationTableFieldService.updateBatchById(fieldList);
                }
                //
                String conditionJson = piAllocationTableField.getConditionJson();
                //json 转 数组对象PiAllocationTableCondition
                List<PiAllocationTableCondition> allocationTableConditionList = JSONObject.parseArray(conditionJson, PiAllocationTableCondition.class);
                for (PiAllocationTableCondition piAllocationTableCondition : allocationTableConditionList) {
                    piAllocationTableCondition.setAllocationTableId(allocationTableId);
                    piAllocationTableCondition.setTableId(tableId);
                    piAllocationTableCondition.setCorrelation(correlation);
                }
                //allocationTableConditionList 获取id集合
                List<String> conditionIds = allocationTableConditionList.stream().map(PiAllocationTableCondition::getId).collect(Collectors.toList());
                iPiAllocationTableConditionService.remove(new LambdaQueryWrapper<PiAllocationTableCondition>()
                        .eq(PiAllocationTableCondition::getAllocationTableId,allocationTableId).notIn(PiAllocationTableCondition::getId,conditionIds));

                //获取条件数据
                iPiAllocationTableConditionService.saveOrUpdateBatch(allocationTableConditionList);

                //生成视图
                LambdaQueryWrapper<PiAllocationTableField> tableFieldqueryWrapper = new LambdaQueryWrapper<>();
                tableFieldqueryWrapper.eq(PiAllocationTableField::getAllocationTableId,allocationTableId);
                List<PiAllocationTableField> piAllocationTableFieldList = iPiAllocationTableFieldService.list(tableFieldqueryWrapper);
                //视图名称 "multiple_view_"+分配表id
                String viewName = "multiple_view_"+allocationTableId;;

                //字段集合
                StringBuffer fieldBuff = new StringBuffer();
                fieldBuff.append("0 id, now() as create_time,");

                for (int i = 0; i < piAllocationTableFieldList.size(); i++) {
                    PiAllocationTableField piAllocationTableTableField = piAllocationTableFieldList.get(i);
                    if (i > 0) {
                        fieldBuff.append(", ");
                    }
                    String fieldNameStr = piAllocationTableTableField.getFieldName();
                    String[]  fieldNameArr = fieldNameStr.split("_f_");
                    String fieldStr = fieldNameArr[1];
                    // 1 id,now() create_time,表a.字段a as 表a-字段a (每张表id)
                    fieldBuff.append(piAllocationTableTableField.getTableName()).append(".")
                            .append(fieldStr).append(" as ")
                            .append(piAllocationTableTableField.getTableName()).append("_f_")
                            .append(fieldStr);
                }
                //表名(集合)
                String tableNames = piAllocationTableField.getTableNames();
                String[] tableName = tableNames.split(",");
                //表删除标识集合
                StringBuffer tableDelBuff = new StringBuffer();
                // 表名a 表名a , 表名b 表名b , 表名c 表名c
                StringBuffer tableNameBuff = new StringBuffer();
                //判断是内连接还是外连接
                if(correlation.equals(PiCommonConstants.ONE_STR)) {
                    //内连接
                    for (int i = AllocationTable.ZERO_INT; i < tableName.length; i++) {
                        if (i > 0) {
                           //tableNameBuff.append(" INNER JOIN ");
                           tableNameBuff.append(" , ");
                        }
                        tableNameBuff.append(tableName[i]).append(" ").append(tableName[i]);

                        if (tableDelBuff.length() > AllocationTable.ZERO_INT) {
                            tableDelBuff.append(" and ");
                        }
                        tableDelBuff.append(tableName[i]).append(".del_flag").append(" = 0 ");
                        //字段中添加每张表id用于数据修改使用
                        fieldBuff.append(",").append(tableName[i]).append(".").append("id").append(" as ").append(tableName[i]).append("_f_id");
                        //取第一种表拼接删除标记 del_flag
                        if (i == AllocationTable.ZERO_INT) {
                            fieldBuff.append(",").append(tableName[i]).append(".del_flag");
                        }
                    }
                    String tableNameAll = tableNameBuff.toString();


                    // 条件(表a.f1 = 表b.f2 and 表c.f3 = 表2.f4)  特殊情况  添加or  [and ( 条件1 or 条件2 or 条件3)]
                    StringBuffer conditionBuff = new StringBuffer();
                    //删除标识
                    conditionBuff.append(tableDelBuff);
                    // OR条件分组 集合
                    Map<String, List<PiAllocationTableCondition>> conditionMap = new HashMap<>();
                    for (PiAllocationTableCondition condition : allocationTableConditionList) {
                        //判断分组
                        if (condition.getRelationship().equals(PiCommonConstants.ONE_STR)) {
                            //与(and)
                            conditionBuff.append(" and ");
                            conditionBuff.append(condition.getTableName()).append(".").append(condition.getTableField()).append(" = ");
                            conditionBuff.append(condition.getCorrelationTableName()).append(".").append(condition.getCorrelationTableField());
                        } else if (condition.getRelationship().equals(PiCommonConstants.TWO_STR) && StrUtil.isNotBlank(condition.getGroupCode())) {
                            //或(or)
                            conditionMap.computeIfAbsent(condition.getGroupCode(), k -> new ArrayList<>())
                                    .add(condition);
                        }
                    }
                    //拼接Or条件
                    for (Map.Entry<String, List<PiAllocationTableCondition>> entry : conditionMap.entrySet()) {
                        List<PiAllocationTableCondition> conditionList = entry.getValue();
                        if (conditionList == null || conditionList.size() == AllocationTable.ZERO_INT) {
                            continue;
                        }
                        conditionBuff.append(" and (");
                        // 使用标志位判断是否是第一个条件，避免多余的 OR
                        boolean isFirstCondition = true;
                        for (PiAllocationTableCondition condition : conditionList) {
                            if (!isFirstCondition) {
                                conditionBuff.append(" or ");
                            }
                            conditionBuff.append(condition.getTableName()).append(".").append(condition.getTableField()).append(" = ");
                            conditionBuff.append(condition.getCorrelationTableName()).append(".").append(condition.getCorrelationTableField());
                            isFirstCondition = false;
                        }
                        conditionBuff.append(" ) ");
                    }
                    piAllocationTableFieldMapper.view(viewName, fieldBuff.toString(), tableNameAll, conditionBuff.toString());
                }else{
                    //左外连接

                    //处理关联条件
                    //表对应的条件
                    Map<String, String> conditionMap = new HashMap<>();
                    StringBuffer tableOnBuff = new StringBuffer();
                    for (PiAllocationTableCondition condition : allocationTableConditionList) {
                        //conditionBuff 清空
                        tableOnBuff.setLength(AllocationTable.ZERO_INT);
                        //判断分组
                        if (condition.getRelationship().equals(PiCommonConstants.ONE_STR)) {
                            tableOnBuff.append(condition.getTableName()).append(".").append(condition.getTableField()).append(" = ");
                            tableOnBuff.append(condition.getCorrelationTableName()).append(".").append(condition.getCorrelationTableField());

                            //String conditionTableName = condition.getTableName();
//                            if(conditionMap.containsKey(conditionTableName) && StrUtil.isNotBlank(conditionMap.get(conditionTableName))){
//                                conditionBuff.insert(0," and ");
//                                conditionBuff.insert(0, conditionMap.get(conditionTableName));
//                                conditionMap.put(conditionTableName,conditionBuff.toString());
//                            }else{
//                                conditionMap.put(conditionTableName,conditionBuff.toString());
//                            }
                            String correlationTableName = condition.getCorrelationTableName();
                            if(conditionMap.containsKey(correlationTableName) && StrUtil.isNotBlank(conditionMap.get(correlationTableName))){
                                tableOnBuff.insert(0," and ");
                                tableOnBuff.insert(0, conditionMap.get(correlationTableName));
                                //将删除标记拼接到关联中, where 中会导致关联不上的数据不显示
                                tableOnBuff.append(" and ").append(correlationTableName).append(".del_flag = 0");
                                conditionMap.put(correlationTableName,tableOnBuff.toString());
                            }else{
                                conditionMap.put(correlationTableName,tableOnBuff.toString());
                            }

                        }
                    }

                    StringBuffer conditionBuff = new StringBuffer();
                    //内连接
                    for (int i = AllocationTable.ZERO_INT; i < tableName.length; i++) {
                        if (i > 0) {
                            tableNameBuff.append(" LEFT JOIN ");
                        }
                        tableNameBuff.append(tableName[i]).append(" ").append(tableName[i]);
                        if(i > 0){
                            //连接条件
                            tableNameBuff.append(" on ");
                            tableNameBuff.append(conditionMap.get(tableName[i]));
                        }
                        // 删除表示
//                        if (tableDelBuff.length() > AllocationTable.ZERO_INT) {
//                            tableDelBuff.append(" and ");
//                        }
//                        tableDelBuff.append(tableName[i]).append(".del_flag").append(" = 0 ");
                        if(i == AllocationTable.ZERO_INT){
                            tableDelBuff.append(tableName[i]).append(".del_flag").append(" = 0 ");
                        }
                        //字段中添加每张表id用于数据修改使用
                        fieldBuff.append(",").append(tableName[i]).append(".").append("id").append(" as ").append(tableName[i]).append("_f_id");
                        //取第一种表拼接删除标记 del_flag
                        if (i == AllocationTable.ZERO_INT) {
                            fieldBuff.append(",").append(tableName[i]).append(".del_flag");
                        }
                    }
                    String tableNameAll = tableNameBuff.toString();
                    conditionBuff.append(tableDelBuff);
                    piAllocationTableFieldMapper.view(viewName, fieldBuff.toString(), tableNameAll, conditionBuff.toString());
                }

                //视图名称写入分配主表
                PiAllocationTable piAllocationTable = new PiAllocationTable();
                piAllocationTable.setViewName(viewName);
                piAllocationTable.setId(allocationTableId);
                iPiAllocationTableService.updateById(piAllocationTable);
            }
        }
    }


    /**
     * 查询报表数据(分配报表)
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Override
    public Map<String, Object> queryData(Integer pageNo, Integer pageSize, HttpServletRequest req,PiAllocationTableField piAllocationTableField) {
        Map<String,Object> mapR = new HashMap<>();
        List<Map<String,Object>> list = new ArrayList<>();
        int tatol = 0;
        //pi标准维护值
        List<PiStandardMaintenance> piStandardMaintenanceList = new ArrayList<>();
        //自定义报表id
        String tableId = piAllocationTableField.getTableId();
        //分配报表id
        String allocationTableId = piAllocationTableField.getAllocationTableId();
        if(StrUtil.isNotBlank(tableId) && StrUtil.isNotBlank(allocationTableId)){
            //获取自定义报表
            PiCustomizeTable piCustomizeTable = iPiCustomizeTableService.getById(tableId);
            //获取分配主表
            PiAllocationTable piAllocationTable = iPiAllocationTableService.getById(allocationTableId);
            //获取查询条件(分配表中的字段)
            LambdaQueryWrapper<PiAllocationTableField> queryWrapper = new LambdaQueryWrapper<PiAllocationTableField>();
            queryWrapper.eq(PiAllocationTableField::getAllocationTableId,allocationTableId);
//            queryWrapper.eq(PiAllocationTableField::getIsQuery,"1");
            queryWrapper.eq(PiAllocationTableField::getIsList,"1");
            queryWrapper.orderByAsc(PiAllocationTableField::getSort);
            List<PiAllocationTableField> fieldList = list(queryWrapper);

            //参数
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, String[]> parameterMap = req.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                if (values.length == 1) {
                    paramMap.put(key, values[0]);
                } else {
                    paramMap.put(key, values);
                }
            }

            //map存储 key字段-value PiAllocationTableField对象
            Map<String,PiAllocationTableField> dataFormatMap= new HashMap<>();
            //查询条件
            StringBuffer conditionBuff = new StringBuffer();
            //返回字段
            StringBuffer fieldBuff = new StringBuffer();
            fieldBuff.append(" id ");
            //排序集合
            StringBuffer sortBuff = new StringBuffer();


            // 数据过滤条件
             String conditionJson = piAllocationTable.getConditionJson();
            if(StrUtil.isNotBlank(conditionJson) && !"[]".equals(conditionJson)){
                conditionBuff.append(" and ").append(ConditionParser.parseCondition(conditionJson));
            }
            //id查询
            if(StrUtil.isNotBlank(piAllocationTableField.getId()) && StrUtil.isBlank(piAllocationTableField.getUpOrDown())){
                conditionBuff.append(" and id = '").append(piAllocationTableField.getId()).append("' ");
            }

            //遍历字段
            for (PiAllocationTableField tableField : fieldList) {
                //如果重复引用字段不为空，只查询唯一字段和重复引用字段
                if(StrUtil.isNotBlank(piAllocationTableField.getCiteField())
                    && !piAllocationTableField.getCiteField().equals(tableField.getFieldName())
                    && tableField.getIsUnique() != 1
                ){
                    continue;
                }
                dataFormatMap.put(tableField.getFieldName(),tableField);

                String fieldName = tableField.getFieldName();
                //过滤查询条件
                if(StrUtil.isNotBlank(paramMap.get(tableField.getFieldName())+"") && !"null".equals(paramMap.get(tableField.getFieldName())+"") && tableField.getIsQuery() == 1){
                    //sql注入检查
                    SqlInjectionUtil.filterContent(paramMap.get(tableField.getFieldName()).toString());
                    //4 日期(Date)  6时间(DateTime)
                    if("4".equals(tableField.getType()) || "6".equals(tableField.getType())){
                        //时间
                        String[] datas = paramMap.get(tableField.getFieldName()).toString().split(",");
                        if(datas.length == 2){
                            conditionBuff.append(" and ").append(tableField.getFieldName()).append(" >= '").append(datas[0]).append("' ");
                            conditionBuff.append(" and ").append(tableField.getFieldName()).append(" <= '").append(datas[1]).append("' ");
                        } else if (datas.length == 1){
                            conditionBuff.append(" and ").append(tableField.getFieldName()).append(" = '").append(datas[0]).append("' ");
                        }
                    }else{
                        //是否模糊查询
                        if(tableField.getIsLike() == 1){
                            conditionBuff.append(" and ").append(tableField.getFieldName()).append(" like '%").append(paramMap.get(tableField.getFieldName())+"").append("%' ");
                        }else{
                            conditionBuff.append(" and ").append(tableField.getFieldName()).append(" = '").append(paramMap.get(tableField.getFieldName())+"").append("' ");
                        }
                    }
                }

                //获取需要显示的字段
                //获取需要格式化数据(日期/时间/代码)
                if("4".equals(tableField.getType())){
                    //日期
                    if (DataBaseTypeUtil.getDbType() == DbType.MYSQL) {
                        fieldBuff.append(" , ").append(" DATE_FORMAT( ").append(fieldName).append(", '%Y-%m-%d') as ").append(fieldName);
                    } else if (DataBaseTypeUtil.getDbType() == DbType.POSTGRE_SQL) {
                        fieldBuff.append(" , ").append(" to_char( ").append(fieldName).append(", 'YYYY-MM-DD') as ").append(fieldName);
                    }

                }else if("6".equals(tableField.getType())){
                    //时间
                    if (DataBaseTypeUtil.getDbType() == DbType.MYSQL) {
                        fieldBuff.append(" , ").append(" DATE_FORMAT( ").append(fieldName).append(", '%Y-%m-%d %H:%i:%s') as ").append(fieldName);
                    } else if (DataBaseTypeUtil.getDbType() == DbType.POSTGRE_SQL) {
                        fieldBuff.append(" , ").append(" to_char( ").append(fieldName).append(", 'YYYY-MM-DD HH24:MI:SS') as ").append(fieldName);
                    }
                }else{
                    //其他类型
                    fieldBuff.append(" , ").append(fieldName);
                }
                //获取排序集合
                if(1 == tableField.getIsSort()){
                    //排序方式
                    String sortType = tableField.getSortType() == 1?" asc ":" desc ";
                    if(null != sortBuff && sortBuff.length() == 0){
                        sortBuff.append(" order by ").append(fieldName).append(sortType);
                    }else{
                        sortBuff.append(" , ").append(fieldName).append(sortType);
                    }
                }

            }
            //自定义报表查询上下页
            if(StrUtil.isNotBlank(piAllocationTableField.getUpOrDown())){
                // 解析当前排序字段的值
                String currentSortValuesJson = piAllocationTableField.getCurrentSortValues();
                Map<String, Object> currentSortValues = new HashMap<>();
                if(StrUtil.isNotBlank(currentSortValuesJson)){
                    try {
                        currentSortValues = JSONObject.parseObject(currentSortValuesJson, Map.class);
                    } catch (Exception e) {
                        // 解析失败，使用空map
                    }
                }

                // 收集排序字段信息
                List<Map<String, Object>> sortFieldsList = new ArrayList<>();
                for (PiAllocationTableField tableField : fieldList) {
                    if(1 == tableField.getIsSort()){
                        Map<String, Object> sortInfo = new HashMap<>();
                        sortInfo.put("fieldName", tableField.getFieldName());
                        sortInfo.put("sortType", tableField.getSortType() == 1 ? "asc" : "desc");
                        sortInfo.put("currentValue", currentSortValues.get(tableField.getFieldName()));
                        sortFieldsList.add(sortInfo);
                    }
                }

                if(sortFieldsList.isEmpty()){
                    // 没有配置排序字段，降级使用id
                    if("0".equals(piAllocationTableField.getUpOrDown())){
                        conditionBuff.append(" and id > ").append(piAllocationTableField.getId()).append(" ");
                        sortBuff = new StringBuffer();
                        sortBuff.append(" ORDER BY id ASC ");
                    }else {
                        conditionBuff.append(" and id < ").append(piAllocationTableField.getId()).append(" ");
                        sortBuff = new StringBuffer();
                        sortBuff.append(" ORDER BY id DESC ");
                    }
                } else {
                    // 基于排序字段构建条件
                    boolean isNextBatch = "0".equals(piAllocationTableField.getUpOrDown());
                    String whereClause = buildSortFieldCondition(sortFieldsList, isNextBatch);
                    if(StrUtil.isNotBlank(whereClause)){
                        conditionBuff.append(" and (").append(whereClause).append(") ");
                    }
                    sortBuff = new StringBuffer();
                    sortBuff.append(buildSortFieldOrderBy(sortFieldsList, isNextBatch));
                }
            }

            //排序为空设置默认值
            if(null != sortBuff && sortBuff.length() == 0){
                sortBuff.append(" order by  create_time desc ");
            }
            //重复引用字段不为空，添加查询条件需要有5w的数据
            if(StrUtil.isNotBlank(piAllocationTableField.getCiteField())){
                conditionBuff.append(" and exists ( " )
                        .append(" select 1 from mes3_five_why_analyse fw where fw.del_flag = 0 ")
                        .append(" and fw.data_id = ").append(piAllocationTableField.getTableName()).append(".id ")
                        .append(" and fw.table_code = '").append(piAllocationTableField.getTableName()).append("' ")
                        .append(" and fw.field_code = '").append(piAllocationTableField.getCiteField()).append("'")
                        .append(" ) ");
            }


            //查询条件
            paramMap.put("condition",conditionBuff.toString());
            //查询表
            String tableName = piCustomizeTable.getTableName();
            if(AllocationTable.MULTIPLETABLES_YES.equals(piAllocationTable.getMultipleTables())){
                String[] tableNames = piAllocationTable.getTableName().split(",");
                //多表
                if(StrUtil.isNotBlank(piAllocationTable.getViewName())){
                    //视图
                    tableName = piAllocationTable.getViewName();
                }
                //添加id字段
                //遍历 tableNames 拼接id
                for (String name : tableNames) {
                    fieldBuff.append(" , ").append(name).append("_f_id ");
                }
//                fieldBuff.append(",").append("mes3_basedata_006_f_id ");
//                fieldBuff.append(",").append("mes3_basedata_005_f_id ");
            }else{
                //无限扩展
                if(StrUtil.isNotBlank(piCustomizeTable.getViewName())){
                    //视图
                    tableName = piCustomizeTable.getViewName();
                }
            }
            //查询字段
            paramMap.put("query",fieldBuff.toString());

            paramMap.put("tableName", tableName);
            //排序(默认创建时间)
            paramMap.put("sort",sortBuff.toString());

            //查询总数
            tatol = piAllocationTableFieldMapper.getTableDataCount(paramMap);
            if(tatol > 0){
                //获取红黄绿标准数据结果
                LambdaQueryWrapper<PiStandardData> standardDataQueryWrapper = new LambdaQueryWrapper<>();
                standardDataQueryWrapper.eq(PiStandardData::getTableName, piCustomizeTable.getTableName());
                List<PiStandardData> piStandardDataList = iPiStandardDataService.list(standardDataQueryWrapper);

                //获取标准数据管理id集合(去重),getManageId存储的是以逗号分隔的字符串
                List<String> distinctManageIdList = piStandardDataList.stream()
                        .map(PiStandardData::getManageId)
                        .distinct()
                        .collect(Collectors.toList());

                List<String> ManageIds = new ArrayList<>();
                for (String distinctManageIdStr : distinctManageIdList){
                    if(null != distinctManageIdStr){
                        List<String> manageList = Arrays.stream(distinctManageIdStr.split(",")).distinct().toList();
                        ManageIds.addAll(manageList);
                    }
                }
                if(null != ManageIds && ManageIds.size() > 0){
                    //根据标准id获取标准维护pi标准值
                    LambdaQueryWrapper<PiStandardMaintenance> maintenanceQueryWrapper = new LambdaQueryWrapper<>();
                    maintenanceQueryWrapper.in(PiStandardMaintenance::getManageId,ManageIds);
                    piStandardMaintenanceList = iPiStandardMaintenanceService.list(maintenanceQueryWrapper);
                }

                //分页查询
                paramMap.put("pageNo", (pageNo-1) * pageSize);
                paramMap.put("pageSize", pageSize);
                list = piAllocationTableFieldMapper.getTableData(paramMap);

                //比对5W和修改记录
                List<Long> idList = list.stream()
                        .filter(map -> map.containsKey("id")) // 过滤出包含 "id" 键的 Map
                        .map(map -> {
                            Object idObj = map.get("id");
                            if (idObj instanceof Number) {
                                return ((Number) idObj).longValue();
                            } else if (idObj instanceof String) {
                                return Long.parseLong((String) idObj);
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                
                //查询5W分析数据
                LambdaQueryWrapper<FiveWhyAnalyse> maintenanceQueryWrapper = new LambdaQueryWrapper<>();
                maintenanceQueryWrapper.in(FiveWhyAnalyse::getDataId,idList);
                maintenanceQueryWrapper.eq(FiveWhyAnalyse::getTableCode,piCustomizeTable.getTableName());
                List<FiveWhyAnalyse> fiveList = fiveWhyAnalyseService.list(maintenanceQueryWrapper);
                
                //查询修改记录
                LambdaQueryWrapper<PiApprove> approveQueryWrapper = new LambdaQueryWrapper<>();
                approveQueryWrapper.in(PiApprove::getDataId,idList);
                approveQueryWrapper.eq(PiApprove::getTableName,piCustomizeTable.getTableName());
                approveQueryWrapper.eq(PiApprove::getDelFlag,0);
                approveQueryWrapper.eq(PiApprove::getState,0);
                // 排除admin和fadmin的修改
                approveQueryWrapper.notIn(PiApprove::getUpdateBy, "admin", "fadmin");
                List<PiApprove> approveList = piApproveService.list(approveQueryWrapper);
                
                //构建修改记录映射，key为dataId，value为修改的字段列表
                Map<String, Set<String>> modifiedMap = new HashMap<>();
                for (PiApprove approve : approveList) {
                    String dataId = approve.getDataId();
                    String fields = approve.getFields();
                    if (StrUtil.isNotBlank(fields)) {
                        Set<String> fieldSet = modifiedMap.getOrDefault(dataId, new HashSet<>());
                        String[] fieldArray = fields.split(",");
                        for (String field : fieldArray) {
                            fieldSet.add(field.trim());
                        }
                        modifiedMap.put(dataId, fieldSet);
                    }
                }

                //数据格式化
                for (Map<String, Object> map : list) {
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        PiAllocationTableField allocationTableField = dataFormatMap.get(key);
                        if(null != allocationTableField){
                            //取值方式
                            if(StrUtil.isNotBlank(allocationTableField.getTakingValue())){
                                //判断是否数字
                                if(NumberUtil.isNumber(value+"") && !value.toString().contains("l") && !value.toString().contains("L")) {
                                    //Double numBer = Double.parseDouble(value + "");
                                    BigDecimal decimalValue = new BigDecimal(String.valueOf(value));
                                    map.put(key,processTakingValue(decimalValue, allocationTableField.getTakingValue(), allocationTableField.getKeepDecimals()));
//                                    if ("1".equals(allocationTableField.getTakingValue())) {
//                                        //1向上取整
//                                        map.put(key,Math.ceil(numBer));
//                                    } else if ("2".equals(allocationTableField.getTakingValue())) {
//                                        //2向下取整
//                                        map.put(key,Math.floor(numBer));
//                                    } else if ("3".equals(allocationTableField.getTakingValue())) {
//                                        //3四舍五入
//                                        //map.put(key,Math.round(numBer));
//                                        BigDecimal bd = new BigDecimal(String.valueOf(numBer));
//                                        // 设置保留4位小数并使用四舍五入模式
//                                        //判断保留小数位
//                                        int number = 4;
//                                        if(null != allocationTableField.getKeepDecimals()){
//                                            number = allocationTableField.getKeepDecimals();
//                                        }
//                                        bd = bd.setScale(number, RoundingMode.HALF_UP);
//                                        map.put(key,bd.doubleValue());
//                                    }
                                }
                            }
                            //保留几位小数
                            if(null != allocationTableField.getKeepDecimals()){
                                //取值方式后重新获取最新值进行处理
                                value = map.get(key);
                                //判断是否数字
                                if(NumberUtil.isNumber(value+"") && !value.toString().contains("l") && !value.toString().contains("L")){
                                    String str = "";
                                    if("0".equals(allocationTableField.getKeepDecimals()+"")){
                                        //保留整数
                                        str = "#0";
                                    }else{
                                        //保留n位小数
                                        str = "#0.";
                                        for (int i = 0; i < allocationTableField.getKeepDecimals(); i++) {
                                            str += 0;
                                        }
                                    }
                                    if("4".equals(allocationTableField.getTakingValue())){
                                        //截取
                                        DecimalFormat df = new DecimalFormat(str);
                                        df.setRoundingMode(RoundingMode.DOWN);
                                        map.put(key, df.format(Double.parseDouble(value+"")));
                                    }else{
                                        //保留小数,并且进行四舍五入
                                        Double numBer = Double.parseDouble(value + "");
                                        map.put(key,new DecimalFormat(str).format(numBer));
                                    }

                                }

                            }
                        }
                    }

                    //比对获取标准数据红黄绿计算结果
                    String standardJson = "";
                    for(PiStandardData piStandardData: piStandardDataList){
                        if(MapUtil.getStr(map, "id").equals(piStandardData.getDataId())){
                            standardJson = piStandardData.getStandardJson();
                            break;
                        }
                    }
                    map.put("piStandardData", standardJson);
                    if(StrUtil.isNotBlank(standardJson)){
                        //通过standardJson，计算出每个PI的标准值和偏差值
                        List<Map> listMapHave = JSONUtil.toList(standardJson, Map.class);
                        for(Map jsonMap : listMapHave){
                            if(null != jsonMap.get("maintenance_id") && !"".equals(jsonMap.get("maintenance_id"))){
                                //获取标准ID
                                String maintenanceId = jsonMap.get("maintenance_id").toString();
                                Optional<PiStandardMaintenance> maintenanceOptional = piStandardMaintenanceList.stream().filter(e->e.getId().equals(maintenanceId)).findFirst();
                                if(maintenanceOptional.isPresent()){
                                    //pi标准字段，规则是pi字段名称加上"_2"
                                    String standarField = maintenanceOptional.get().getFieldName() + "_2";
                                    //pi标准值
                                    Double standarValue = maintenanceOptional.get().getStandardValue();
                                    map.put(standarField,standarValue);
                                    //pi偏差值
                                    if(null != map.get(maintenanceOptional.get().getFieldName()) && !"".equals(map.get(maintenanceOptional.get().getFieldName()))){
                                        String actualValueStr = map.get(maintenanceOptional.get().getFieldName()).toString();
                                        try {
                                            Double actualValue = Double.parseDouble(actualValueStr);
                                            if(null != standarValue){
                                                Double difference = NumberUtil.sub(actualValue,standarValue);
                                                map.put(maintenanceOptional.get().getFieldName()+"_3",difference);
                                            }
                                        }catch (Exception e){
                                            map.put(maintenanceOptional.get().getFieldName()+"_3","");
                                        }
                                    }
                                }
                            }
                        }
                    }

                    String id = map.get("id")+"";
                    //判断是否5W
                    for (FiveWhyAnalyse fiveWhyAnalyse : fiveList) {
                        if(id.equals(fiveWhyAnalyse.getDataId())){
                            map.put(fiveWhyAnalyse.getFieldCode()+"_f",true);
                            //异常分析-重复引用功能，直接返回对象
                            if(StrUtil.isNotBlank(piAllocationTableField.getCiteField())
                                    && fiveWhyAnalyse.getFieldCode().equals(piAllocationTableField.getCiteField())){
                                map.put(fiveWhyAnalyse.getFieldCode()+"_f", fiveWhyAnalyse);
                            }
                        }
                    }
                    
                    //添加修改的字段列表
                    Set<String> modifiedFields = modifiedMap.get(id);
                    map.put("modifiedFields", modifiedFields != null ? modifiedFields : new HashSet<>());

                }
            }
        }
        mapR.put("records",list);
        mapR.put("total",tatol);
        return mapR;
    }

    /**
     * 处理取值方式
     * @param value 值
     * @param takingValue  取值方式
     * @param keepDecimals 保留小数位
     * @return
     */
    private BigDecimal processTakingValue(BigDecimal value, String takingValue, Integer keepDecimals) {
        switch (takingValue) {
            case "1": // 向上取整
                return value.setScale(0, RoundingMode.CEILING);
            case "2": // 向下取整
                return value.setScale(0, RoundingMode.FLOOR);
            case "3": // 四舍五入，保留小数位
                int decimals = keepDecimals != null ? keepDecimals : 4;
                return value.setScale(decimals, RoundingMode.HALF_UP);
            default:
                return value;
        }
    }


    /**
     * 更新报表值
     * @param map
     */
    @Override
    public Boolean editVal(Map map) {
        Boolean isOk = false;
        try {
            //获取报表名称
            PiAllocationTable piAllocationTable = iPiAllocationTableService.getById(map.get("tableId")+"");
            if(null != piAllocationTable){
                String value = map.get("value")+"";
                //值验证是否存在sql注入
                SqlInjectionUtil.filterContent(value);
                //更新字段
                List<TableFieldValueDto> fieldList = new ArrayList<>();
                TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
                tableFieldValueDto.setFieldVal(value);
                tableFieldValueDto.setField(map.get("key")+"");
                fieldList.add(tableFieldValueDto);

                //条件
                List<TableConditionDto> conditionList = new ArrayList<>();
                TableConditionDto tableConditionDto = new TableConditionDto();
                tableConditionDto.setKey("id");
                tableConditionDto.setVal(map.get("id")+"");
                conditionList.add(tableConditionDto);

                //更新值
                TableSaveDto tableSaveDto = new TableSaveDto();
                tableSaveDto.setTable(piAllocationTable.getTableName());
                tableSaveDto.setConditionList(conditionList);
                tableSaveDto.setFieldList(fieldList);
                //更新
                String re = iPiCustomizeTableService.tableSave(tableSaveDto);
                if("0".equals(re)){
                    //更新成功
                    isOk = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOk;
    }

    /**
     * PI日报批量更新/新增
     * @param map
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String,String> batchEditVal(Map map) {
        Map<String, String> mapR = new HashMap<>();
        mapR.put("code", "0");
        //表名
        String tableName = map.get("tableName") + "";
        //id集合
        List<String> dataIds = new ArrayList<>();
        //被修改字段集合
        String editFields = map.get("editFields") + "";
        //数据集合
        ArrayList<LinkedHashMap> dataList = (ArrayList<LinkedHashMap>) map.get("data");
        if (null == dataList || (null != dataList && dataList.size() == 0)) {
            //保存数据为空
            mapR.put("code", "3");
        }

        boolean isMultiple = false;
        if ("2".equals(map.get("type"))) {
            PiAllocationTable piAllocationTable = iPiAllocationTableService.getById(map.get("allocationTableId")+"");
            if(null != piAllocationTable){
                isMultiple = "1".equals(piAllocationTable.getMultipleTables());
            }
        }

        if(!isMultiple){
            //单表
            for (LinkedHashMap linkedHashMap : dataList) {
                String id = linkedHashMap.get("id") + "";
                if (StrUtil.isNotBlank(id)) {
                    //id集合
                    dataIds.add(id);
                    //更新字段集合
                    Set<String> fieldNameSet = new HashSet<>();
                    //判断PI报表还是基础报表
                    if ("1".equals(map.get("type"))) {
                        //基础报表
                        LambdaQueryWrapper<PiCustomizeTableField> fieldWrapper = new LambdaQueryWrapper<>();
                        fieldWrapper.eq(PiCustomizeTableField::getTableName, tableName);
                        fieldWrapper.eq(PiCustomizeTableField::getIsEdit, "1");
                        List<PiCustomizeTableField> customizeFieldList = iPiCustomizeTableFieldService.list(fieldWrapper);

                        fieldNameSet = customizeFieldList.stream()
                                .map(PiCustomizeTableField::getFieldName)
                                .collect(Collectors.toSet());
                    } else {
                        //PI报表
                        String allocationTableId = map.get("allocationTableId") + "";
                        LambdaQueryWrapper<PiAllocationTableField> fieldWrapper = new LambdaQueryWrapper<>();
                        //fieldWrapper.eq(PiAllocationTableField::getTableName,tableName);
                        fieldWrapper.eq(PiAllocationTableField::getAllocationTableId, allocationTableId);
                        fieldWrapper.eq(PiAllocationTableField::getIsEdit, "1"); //可编辑
                        List<PiAllocationTableField> customizeFieldList = iPiAllocationTableFieldService.list(fieldWrapper);

                        fieldNameSet = customizeFieldList.stream()
                                .map(PiAllocationTableField::getFieldName)
                                .collect(Collectors.toSet());
                    }

                    List<TableFieldValueDto> fieldList = new ArrayList<>();
                    Set<String> keySet = linkedHashMap.keySet();
                    for (String key : keySet) {
                        //判断字段是否可以编辑
                        if (key.contains("field_") && fieldNameSet.contains(key)) {
                            Object value = linkedHashMap.get(key);
                            TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
                            if (null != value && StrUtil.isNotBlank(value + "") && !"/".equals(value + "")) {
                                //清除前后空格
                                value = value.toString().trim();
                                tableFieldValueDto.setFieldVal(value.toString());
                            } else {
                                tableFieldValueDto.setFieldVal(null);
                            }
                            tableFieldValueDto.setField(key);
                            fieldList.add(tableFieldValueDto);
                        }
                    }
                    //条件
                    List<TableConditionDto> conditionList = new ArrayList<>();
                    TableConditionDto tableConditionDto = new TableConditionDto();
                    tableConditionDto.setKey("id");
                    tableConditionDto.setVal(id);
                    conditionList.add(tableConditionDto);

                    //更新值
                    TableSaveDto tableSaveDto = new TableSaveDto();
                    tableSaveDto.setTable(tableName);
                    tableSaveDto.setConditionList(conditionList);
                    tableSaveDto.setFieldList(fieldList);
                    //更新
                    String ok = iPiCustomizeTableService.tableSave(tableSaveDto);
                    if ("-1".equals(ok)) {
                        mapR.put("code", "-1");
                    }
                } else {
                    //新增
                    //判断唯一约束是否存在(基础数据表)
                    LambdaQueryWrapper<PiCustomizeTableField> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(PiCustomizeTableField::getTableName, tableName);
                    queryWrapper.eq(PiCustomizeTableField::getIsUnique, "1"); //唯一约束
                    List<PiCustomizeTableField> list = iPiCustomizeTableFieldService.list(queryWrapper);
                    Map<String, PiCustomizeTableField> fieldMap = list.stream()
                            .collect(Collectors.toMap(PiCustomizeTableField::getFieldName, t -> t));
                    boolean isNull = false;
                    //条件(唯一约束)
                    List<TableConditionDto> conditionList = new ArrayList<>();

                    //判断唯一约束是否为空
                    if (fieldMap.isEmpty()) {
                        //默认添加 条件
                        TableConditionDto tableConditionDto = new TableConditionDto();
                        tableConditionDto.setKey("create_time");
                        tableConditionDto.setVal(DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm:ss"));
                        conditionList.add(tableConditionDto);
                    } else {
                        for (Map.Entry<String, PiCustomizeTableField> entry : fieldMap.entrySet()) {
                            //条件
                            TableConditionDto tableConditionDto = new TableConditionDto();
                            tableConditionDto.setKey(entry.getKey());
                            tableConditionDto.setVal(linkedHashMap.get(entry.getKey()) + "");
                            conditionList.add(tableConditionDto);
                            //移除唯一约束(新增会自动拼接)[录入项中的]
                            linkedHashMap.remove(entry.getKey());
                        }
                    }

                    //if(!isNull){
                    //新增(存在进行更新)
                    List<TableFieldValueDto> fieldList = new ArrayList<>();
                    Set<String> keySet = linkedHashMap.keySet();
                    for (String key : keySet) {
                        if (key.contains("field_")) {
                            Object value = linkedHashMap.get(key);
                            TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
                            if (null != value && StrUtil.isNotBlank(value + "")) {
                                tableFieldValueDto.setFieldVal(value + "");
                            } else {
                                tableFieldValueDto.setFieldVal(null);
                            }
                            tableFieldValueDto.setField(key);
                            fieldList.add(tableFieldValueDto);
                        }
                    }
                    //值
                    TableSaveDto tableSaveDto = new TableSaveDto();
                    tableSaveDto.setTable(tableName);
                    tableSaveDto.setConditionList(conditionList);
                    tableSaveDto.setFieldList(fieldList);
                    //保存/更新
                    String rId = iPiCustomizeTableService.tableSave(tableSaveDto);
                    if ("-1".equals(rId)) {
                        mapR.put("code", "-1");
                    } else {
                        //id集合
                        dataIds.add(rId);
                    }
    //                }else{
    //                    //唯一约束为空
    //                    mapR.put("code", "4");
    //                }
                    //linkedHashMap
                }
            }

            if("0".equals(mapR.get("code"))){
                //保存成功-更新PI间数据
                List<String> piCodes = null;
                if(StrUtil.isNotBlank(editFields)){
                    piCodes = Arrays.asList(editFields.split(","));
                }
                savePICalculateSignal(dataIds, tableName,piCodes);
            }
        }else{
            //多表-更新
            //移除id
            dataList.forEach(linkedHashMap -> linkedHashMap.remove("id"));
            for (LinkedHashMap<String, Object> linkedHashMap : dataList) {
//                for (Object key : linkedHashMap.keySet()) {
//                    if (key.contains("_id")) {
//                        idKeys.add(key);
//                    }
//                }
                Map<String, Map<String, Object>> result = new HashMap<>();
                for (Map.Entry<String, Object> entry : linkedHashMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    // 解析前缀和字段名
                    String[] parts = key.split("_f_");
                    if (parts.length == 2) {
                        String prefix = parts[0]; // mes3_basedata_006
                        String fieldName = parts[1]; // id 或 field_0005

                        // 如果前缀不存在，创建新的Map
                        result.putIfAbsent(prefix, new HashMap<>());

                        // 将字段添加到对应的前缀Map中
                        result.get(prefix).put(fieldName, value);
                    }
                }
                for (Map.Entry<String, Map<String, Object>> outerEntry : result.entrySet()) {
                    String table = outerEntry.getKey();
                    Map<String, Object> innerMap = outerEntry.getValue();
                    TableSaveDto tableSaveDto = new TableSaveDto();
                    List<TableFieldValueDto> fieldList = new ArrayList<>();
                    List<TableConditionDto> conditionList = new ArrayList<>();
                    List<String> piCodes = new ArrayList<>();
                    dataIds = new ArrayList<>();
                    for (Map.Entry<String, Object> fieldOuterEntry : innerMap.entrySet()) {
                        String fieldKey = fieldOuterEntry.getKey();
                        Object val = fieldOuterEntry.getValue();

                        if(fieldKey.equals(AllocationTable.ID)){
                            //条件
                            TableConditionDto tableConditionDto = new TableConditionDto();
                            tableConditionDto.setKey("id");
                            if(val != null){
                                tableConditionDto.setVal(val.toString());
                            }else{
                                tableConditionDto.setVal("");
                            }
                            conditionList.add(tableConditionDto);
                            dataIds.add(tableConditionDto.getVal());
                        }else{
                            //更新值
                            //判断字段
                            if (fieldKey.contains("field_")) {
                                TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
                                if (null != val && StrUtil.isNotBlank(val + "")) {
                                    //清除前后空格
                                    val = val.toString().trim();
                                    tableFieldValueDto.setFieldVal(val.toString());
                                } else {
                                    tableFieldValueDto.setFieldVal(null);
                                }
                                tableFieldValueDto.setField(fieldKey);
                                fieldList.add(tableFieldValueDto);
                                piCodes.add(fieldKey);
                            }

                        }
                    }
                    if(conditionList == null || (conditionList != null && conditionList.isEmpty())){
                        continue;
                    }
                    tableSaveDto.setTable(table);
                    tableSaveDto.setConditionList(conditionList);
                    tableSaveDto.setFieldList(fieldList);
                    //更新
                    String ok = iPiCustomizeTableService.tableSave(tableSaveDto);
                    if(!"-1".equals( ok)){
                        //保存成功-更新PI间数据
                        savePICalculateSignal(dataIds, table,piCodes);
                    }
                }
            }
            //判断是否多表 判断editFields是否包含_f_
            //                    if(editFields.contains("_f_")){
            //                        //多表
            //                        // editFields 通过 '_f_' 分割  0为表示字段名称  1为表示字段值
            //                        String[] editFieldsArr = editFields.split("_f_");
            //                        tableName = editFieldsArr[0];
            //                        editFields = editFieldsArr[1];
            //                    }
        }
        return mapR;
    }


    /**
     * 获取PI报表表头
     * @param tableDataDto
     * @return
     */
    @Override
    public List<?> tableHead(TableDataDto tableDataDto) {
        List<?> list = new ArrayList<>();
        if("1".equals(tableDataDto.getType())){
            //基础报表
            list = baseTableHead(tableDataDto.getTableId());
        }else{
            //pi报表
            list = piTableHead(tableDataDto.getTableId());
        }
        return list;
    }

    /**
     * 获取pi报表
     * @return
     */
    public List<?> piTableHead(String tableId){
        List<PiAllocationTableField> listAll = new ArrayList<>();
        //获取pi报表(不包含唯一约束)
        LambdaQueryWrapper<PiAllocationTableField> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PiAllocationTableField::getAllocationTableId, tableId);
		queryWrapper.eq(PiAllocationTableField::getIsList, "1"); //是否列表显示
        queryWrapper.eq(PiAllocationTableField::getIsUnique, "0"); //是否唯一约束(否)
        queryWrapper.eq(PiAllocationTableField::getIsEdit, "1"); //是否可编辑(是)
		queryWrapper.orderByAsc(PiAllocationTableField::getSort);
		List<PiAllocationTableField> list = iPiAllocationTableFieldService.list(queryWrapper);

        //获取基础报表(唯一约束)
        List<PiCustomizeTableField> uniqueList = new ArrayList<>();
        if(null != list && list.size() > 0){
            LambdaQueryWrapper<PiCustomizeTableField> queryUniqueWrapper = new LambdaQueryWrapper<>();
            queryUniqueWrapper.eq(PiCustomizeTableField::getTableId, list.get(0).getTableId());
            queryUniqueWrapper.eq(PiCustomizeTableField::getIsUnique, "1"); //是否唯一约束
            queryUniqueWrapper.orderByAsc(PiCustomizeTableField::getSort);
            uniqueList = iPiCustomizeTableFieldService.list(queryUniqueWrapper);
        }
        //合并
        for (PiCustomizeTableField piCustomizeTableField : uniqueList) {
            PiAllocationTableField piAllocationTableField = new PiAllocationTableField();
            BeanUtil.copyProperties(piCustomizeTableField, piAllocationTableField);
            listAll.add(piAllocationTableField);
        }
        listAll.addAll(list);
        return listAll;
    }


    /**
     * 获取pi报表
     * @return
     */
    public List<?> baseTableHead(String tableId){
        List<PiCustomizeTableField> listAll = new ArrayList<>();
        //获取pi报表(不包含唯一约束)
        LambdaQueryWrapper<PiCustomizeTableField> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PiCustomizeTableField::getTableId, tableId);
        queryWrapper.eq(PiCustomizeTableField::getIsList, "1"); //是否列表显示
        queryWrapper.eq(PiCustomizeTableField::getIsUnique, "0"); //是否唯一约束(否)
        queryWrapper.eq(PiCustomizeTableField::getIsEdit, "1"); //是否可编辑(是)
        queryWrapper.orderByAsc(PiCustomizeTableField::getSort);
        List<PiCustomizeTableField> list = iPiCustomizeTableFieldService.list(queryWrapper);

        //获取基础报表(唯一约束)
        if(null != list && list.size() > 0){
            LambdaQueryWrapper<PiCustomizeTableField> queryUniqueWrapper = new LambdaQueryWrapper<>();
            queryUniqueWrapper.eq(PiCustomizeTableField::getTableId, tableId);
            queryUniqueWrapper.eq(PiCustomizeTableField::getIsUnique, "1"); //是否唯一约束
            queryUniqueWrapper.orderByAsc(PiCustomizeTableField::getSort);
            listAll = iPiCustomizeTableFieldService.list(queryUniqueWrapper);
        }
        listAll.addAll(list);
        return listAll;
    }

    /**
     * 获取配置算法PI(字段)
     * reportType 1基础报表 / 2PI报表
     * @param piAllocationTableField
     * @return
     */
    @Override
    public List<?> getAlgorithmField(PiAllocationTableField piAllocationTableField) {
        List<?> listData = new ArrayList<>();
        List<?> list = new ArrayList<>();
        //判断报表类型
        String reportType = piAllocationTableField.getReportType();
        List<PiAllocationTableField> piList = null;
        if(StrUtil.isNotBlank(reportType)){
            if("1".equals(reportType)){
                //基础报表
                LambdaQueryWrapper<PiCustomizeTableField> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(PiCustomizeTableField::getTableName, piAllocationTableField.getTableName());
                queryWrapper.eq(PiCustomizeTableField::getIsUnique, "0"); //是否唯一约束(否)
                queryWrapper.orderByAsc(PiCustomizeTableField::getSort);
                list = iPiCustomizeTableFieldService.list(queryWrapper);

            }else if("2".equals(reportType)){
                //PI日报
                LambdaQueryWrapper<PiAllocationTableField> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(PiAllocationTableField::getTableName, piAllocationTableField.getTableName());
                queryWrapper.eq(PiAllocationTableField::getAllocationTableId, piAllocationTableField.getAllocationTableId());
                queryWrapper.eq(PiAllocationTableField::getIsUnique, "0"); //是否唯一约束(否)
                queryWrapper.orderByAsc(PiAllocationTableField::getSort);
                list = iPiAllocationTableFieldService.list(queryWrapper);

            }
            //获取PI主表
            PiCustomizeTable piCustomizeTable = iPiCustomizeTableService.getById(piAllocationTableField.getTableId());

            //获取算法主表
            LambdaQueryWrapper<PiCalculateSignal> queryWrapper = new LambdaQueryWrapper<PiCalculateSignal>();
            queryWrapper.eq(PiCalculateSignal::getPiTable,piAllocationTableField.getTableName());
            queryWrapper.eq(PiCalculateSignal::getDelFlag,0);
            List<PiCalculateSignal> calculateSignalList = iPiCalculateSignalService.list(queryWrapper);
            //获取Adapter算法
            LambdaQueryWrapper<PiAdapterAlgorithm> adapterqueryWrapper = new LambdaQueryWrapper<PiAdapterAlgorithm>();
            adapterqueryWrapper.in(PiAdapterAlgorithm::getPiTable,piAllocationTableField.getTableName());
            adapterqueryWrapper.eq(PiAdapterAlgorithm::getDelFlag,0);
            List<PiAdapterAlgorithm> adapterAlgorithmList = iPiAdapterAlgorithmService.list(adapterqueryWrapper);
            //合并到基础
            for (PiAdapterAlgorithm piAdapterAlgorithm : adapterAlgorithmList) {
                PiCalculateSignal piCalculateSignal = new PiCalculateSignal();
                piCalculateSignal.setPiTable(piAdapterAlgorithm.getPiTable());
                piCalculateSignal.setPiCode(piAdapterAlgorithm.getPiCode());
                calculateSignalList.add(piCalculateSignal);
            }

            //判断 PI主表是否配置数据重算时间对照PI 没配置排查PI日报间计算
            if(null == piCustomizeTable || StrUtil.isBlank(piCustomizeTable.getRecalculateTimePi())){
                // 排除PI日报间计算
                //获取calculateSignalList id集合 排除空 且 取值类型为0(stepType = 0)
                List<String> calculateIdList = calculateSignalList.stream()
                        .filter(calculateSignal -> calculateSignal.getId() != null)
                        .filter(calculateSignal -> calculateSignal.getStepType() == null || "0".equals(calculateSignal.getStepType()))
                        .map(PiCalculateSignal::getId)
                        .collect(Collectors.toList());

                // 获取计算模型(PI间计算)
                List<PiComputationModel> modelList = new ArrayList<>();
                if(calculateIdList != null && !calculateIdList.isEmpty()){
                    LambdaQueryWrapper<PiComputationModel> modelQueryWrapper = new LambdaQueryWrapper<PiComputationModel>();
                    modelQueryWrapper.eq(PiComputationModel::getValueSource,PiCommonConstants.PI_DAILY); //1-pi日报
                    modelQueryWrapper.in(PiComputationModel::getCalculateId,calculateIdList);
                    modelQueryWrapper.eq(PiComputationModel::getDelFlag,CommonConstants.NOT_DELETED);
                    modelList = iPiComputationModelService.list(modelQueryWrapper);
                }

                //获取模型 map 中 key 为 calculateId 的值
                Map<String, List<PiComputationModel>> modelMap = modelList.stream()
                        .collect(Collectors.groupingBy(PiComputationModel::getCalculateId));

//                modelMap.forEach((key, value) -> {
//                    PiComputationModel piComputationModel = value.get(0);
//                });
                // 从算法集合中挑选出与模型内 calculateId 一致的算法
                List<PiCalculateSignal> piIntervalList = calculateSignalList.stream()
                        .filter(calculateSignal -> modelMap.containsKey(calculateSignal.getId()))
                        .collect(Collectors.toList());

                // 算法集合得到 配置PI间计算的 piCode集合
                List<String> piCodeList = piIntervalList.stream()
                        .map(PiCalculateSignal::getPiCode)
                        .collect(Collectors.toList());

                // 从算法集合中排除与PI间计算集合 piCode 一致的算法
                calculateSignalList = calculateSignalList.stream()
                        .filter(calculateSignal -> !piCodeList.contains(calculateSignal.getPiCode()))
                        .collect(Collectors.toList());

            }
            //list转map 去除重复的piCode
            BinaryOperator<PiCalculateSignal> mergeFunction = (existing, replacement) -> existing; // 保留第一个元素
            Map<String, PiCalculateSignal> signalMap = calculateSignalList.stream()
                    .collect(Collectors.toMap(PiCalculateSignal::getPiCode, signal -> signal, mergeFunction));


            List<PiAllocationTableField> piAllocationTableFieldList = new ArrayList();
            List<PiCustomizeTableField> piCustomizeTableFieldList = new ArrayList();
            //判断是否字段是否配置算法
            for (Object element : list) {
                if (element instanceof PiCustomizeTableField) {
                    //基础报表
                    PiCustomizeTableField customizeTableField = (PiCustomizeTableField) element;
                    //判断是否配置算法
                    if(signalMap.containsKey(customizeTableField.getFieldName())){
                        //配置算法
                        piCustomizeTableFieldList.add(customizeTableField);
                    }
                } else if (element instanceof PiAllocationTableField) {
                    //PI报表
                    PiAllocationTableField allocationTableField = (PiAllocationTableField) element;
                    //判断是否配置算法
                    if(signalMap.containsKey(allocationTableField.getFieldName())){
                        //配置算法
                        piAllocationTableFieldList.add(allocationTableField);
                    }
                }
            }
            if(null != piAllocationTableFieldList && piAllocationTableFieldList.size() > 0){
                listData = piAllocationTableFieldList;
            }else if(null != piCustomizeTableFieldList && piCustomizeTableFieldList.size() > 0){
                listData = piCustomizeTableFieldList;
            }
        }
        return listData;
    }

    /**
     * 查询数据总数
     * @param paramMap
     * @return
     */
    @Override
    public int getTableDataCount(Map<String, Object> paramMap) {
        return piAllocationTableFieldMapper.getTableDataCount(paramMap);
    }

    /**
     * 分页查询
     * @param paramMap
     * @return
     */
    @Override
    public List<Map<String, Object>> getTableData(Map<String, Object> paramMap) {
        return piAllocationTableFieldMapper.getTableData(paramMap);
    }


    /**
     * 开启/关闭配置算法的编辑状态
     * @param allocationTableId
     * @param isEdit 1开启 0关闭
     */
    @Override
    public void openOrCloseEdit(String allocationTableId, String isEdit) {

        //获取分配的所有字段
        LambdaQueryWrapper<PiAllocationTableField> fieldWrapper = new LambdaQueryWrapper<>();
        fieldWrapper.eq(PiAllocationTableField::getAllocationTableId, allocationTableId);
        fieldWrapper.eq(PiAllocationTableField::getIsEdit, "1".equals(isEdit)? "0" :"1");
        List<PiAllocationTableField> list = list(fieldWrapper);
        if(list != null && !list.isEmpty()){
            //获取表配置算法字段
            String tableName = list.get(0).getTableName();
            LambdaQueryWrapper<PiCalculateSignal> calculateWrapper = new LambdaQueryWrapper<>();
            calculateWrapper.eq(PiCalculateSignal::getPiTable, tableName);
            List<PiCalculateSignal> calculateList = iPiCalculateSignalService.list(calculateWrapper);


            //PI间计算不开启编辑
            if(PiCommonConstants.OPEN.equals(isEdit)){
                //得到算法集合 步骤类型，为瞬时取数0 calculateList
                List<PiCalculateSignal> instantCalculateIdList = calculateList.stream()
                        .filter(model -> model.getStepType().equals(PiCommonConstants.INSTANT))
                        .collect(Collectors.toList());


                List<String> calculateIdList = instantCalculateIdList.stream()
                        .map(model -> model.getId())
                        .collect(Collectors.toList());
                List<PiComputationModel> modelList = new ArrayList();
                if(instantCalculateIdList != null && instantCalculateIdList.size() > 0) {
                    //获取模型(PI间计算)
                    LambdaQueryWrapper<PiComputationModel> modelQueryWrapper = new LambdaQueryWrapper<PiComputationModel>();
                    modelQueryWrapper.eq(PiComputationModel::getValueSource, PiCommonConstants.PI_DAILY); //1-pi日报
                    modelQueryWrapper.in(PiComputationModel::getCalculateId, calculateIdList);
                    modelQueryWrapper.eq(PiComputationModel::getDelFlag, CommonConstants.NOT_DELETED);
                    modelList = iPiComputationModelService.list(modelQueryWrapper);

                }
                if(modelList != null && modelList.size() > 0){
                    //获取模型 map 中 key 为 calculateId 的值
                    Map<String, List<PiComputationModel>> modelMap = modelList.stream()
                            .collect(Collectors.groupingBy(PiComputationModel::getCalculateId));

                    // 从算法集合中挑选出与模型内 calculateId相同的算法
                    List<PiCalculateSignal> piIntervalList = calculateList.stream()
                            .filter(calculateSignal -> modelMap.containsKey(calculateSignal.getId()))
                            .collect(Collectors.toList());

                    // 算法集合得到 配置PI间计算的 piCode集合
                    List<String> piCodeList = piIntervalList.stream()
                            .map(PiCalculateSignal::getPiCode)
                            .collect(Collectors.toList());

                    // 从算法集合中排除与PI间计算集合 piCode 相同的算法
                    calculateList = calculateList.stream()
                            .filter(calculateSignal -> !piCodeList.contains(calculateSignal.getPiCode()))
                            .collect(Collectors.toList());
                }

            }

            // 先创建计算信号的键集合
            Set<String> calcKeys = calculateList.stream()
                    .map(calc -> calc.getPiTable() + "|" + calc.getPiCode())
                    .collect(Collectors.toSet());

            // 筛选匹配项
            List<PiAllocationTableField> result = list.stream()
                    .filter(field -> calcKeys.contains(field.getTableName() + "|" + field.getFieldName()))
                    .peek(field -> field.setIsEdit(isEdit))  // 设置isEdit属性
                    .collect(Collectors.toList());

            //更新数据
            updateBatchById(result);
        }

    }

    /**
     * 获取最新10条数据
     * @param piAllocationTableField
     * @return
     */
    public Map<String, Object> getTableFieldData(PiAllocationTableField piAllocationTableField) {
        Map<String, Object> map = new HashMap<>();
        //获取表名
        String tableName = "";
        try {
            if("1".equals(piAllocationTableField.getReportType())){
                //基础
                LambdaQueryWrapper<PiCustomizeTable> fieldWrapper = new LambdaQueryWrapper<>();
                fieldWrapper.eq(PiCustomizeTable::getId, piAllocationTableField.getTableId());
                List<PiCustomizeTable> list = iPiCustomizeTableService.list(fieldWrapper);
                if(list != null && !list.isEmpty()){
                    tableName = list.get(0).getTableName();
                }
            }else{
                //日报
                LambdaQueryWrapper<PiAllocationTable> fieldWrapper = new LambdaQueryWrapper<>();
                fieldWrapper.eq(PiAllocationTable::getId, piAllocationTableField.getTableId());
                List<PiAllocationTable> list = iPiAllocationTableService.list(fieldWrapper);
                if(list != null && !list.isEmpty()){
                    tableName = list.get(0).getTableName();
                }
            }
            if(StrUtil.isNotBlank(tableName)){
                String fieldStr = piAllocationTableField.getFields();
                String[] fields = fieldStr.split(",");
                for (String field : fields) {
                    if(StrUtil.isBlank(field)){
                        continue;
                    }
                    Map<String, Object> paramMap = new HashMap<>();
                    //查询字段
                    paramMap.put("query", field);
                    //查询条件
                    paramMap.put("condition", " and "+field +" is not null ");
                    //查询表
                    paramMap.put("tableName",tableName);
                    List<Map<String, Object>> list = piAllocationTableFieldMapper.getTableFieldData(paramMap);
                    if(list != null && !list.isEmpty()){
                        // key为字段名，value为值集合 写入 map
                        List<Object> fieldValues = list.stream()
                                .map(entry -> entry.get(field))
                                .filter(Objects::nonNull)  // 过滤空值
                                .collect(Collectors.toList());
                        map.put(field, fieldValues);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 更新PI间数据
     * @param dataIds
     * @param piTable
     * @param piCodes
     */
    public void savePICalculateSignal(List<String> dataIds, String piTable,List<String> piCodes){
        //保存成功-更新PI间数据
        executorService.submit(() -> {
            iPiComputationModelService.savePICalculateSignal(dataIds, piTable,piCodes);
        });
    }

    /**
     * 更新
     * @param piAllocationTableField
     * @return
     */
    @Override
    public Result<?> edit(PiAllocationTableField piAllocationTableField) {
        //获取算法主表
        //是否有PI日报间计算(去掉限制)
//        if(piAllocationTableField.getIsEdit().equals(PiCommonConstants.OPEN) &&
//                iPiCalculateSignalService.isPiOtherCalculation(piAllocationTableField.getTableName(),piAllocationTableField.getFieldName())) {
//            //存在PI间计算
//            return Result.error(PiCommonConstants.PI_CANNOT_EDIT);
//        }
        //更新
        iPiAllocationTableFieldService.updateById(piAllocationTableField);
        return Result.OK(PiCommonConstants.EDIT_SUCCESS);
    }

    /**
     * 构建排序字段的查询条件
     * @param sortFields 排序字段列表 [{fieldName, sortType, currentValue}]
     * @param isNext true=下一批, false=上一批
     * @return SQL条件字符串
     */
    private String buildSortFieldCondition(List<Map<String, Object>> sortFields, boolean isNext) {
        if (sortFields == null || sortFields.isEmpty()) {
            return "";
        }
        return buildRecursiveCondition(sortFields, 0, isNext);
    }

    /**
     * 递归构建多字段排序条件
     * 原理：对于多字段排序(f1 asc, f2 desc)，下一批条件为：
     * (f1 > v1) OR (f1 = v1 AND f2 < v2)
     */
    private String buildRecursiveCondition(List<Map<String, Object>> sortFields, int index, boolean isNext) {
        if (index >= sortFields.size()) {
            return "";
        }

        Map<String, Object> field = sortFields.get(index);
        String fieldName = (String) field.get("fieldName");
        String sortType = (String) field.get("sortType");
        Object currentValue = field.get("currentValue");

        boolean isAsc = "asc".equalsIgnoreCase(sortType);
        // 下一批：升序找小于(列表下一位置)，降序找大于(列表下一位置)
        // 上一批：升序找大于(列表上一位置)，降序找小于(列表上一位置)
        String operator = isNext ? (isAsc ? "<" : ">") : (isAsc ? ">" : "<");

        // 当前值为空，跳过该字段，继续处理下一个
        if (currentValue == null || "".equals(currentValue.toString())) {
            return buildRecursiveCondition(sortFields, index + 1, isNext);
        }

        // 处理字符串类型需要引号，防止SQL注入
        String valueStr = formatValueForSql(currentValue);

        StringBuilder sb = new StringBuilder();

        // 当前字段条件
        sb.append(fieldName).append(" ").append(operator).append(" ").append(valueStr);

        // 递归处理后续字段 (当前字段相等时)
        String nextCondition = buildRecursiveCondition(sortFields, index + 1, isNext);
        if (StrUtil.isNotBlank(nextCondition)) {
            sb.append(" OR (").append(fieldName).append(" = ").append(valueStr)
                    .append(" AND (").append(nextCondition).append("))");
        }

        return sb.toString();
    }

    /**
     * 格式化值为SQL字符串
     */
    private String formatValueForSql(Object value) {
        if (value == null) {
            return "NULL";
        }
        // 转义单引号防止SQL注入
        String strValue = value.toString().replace("'", "''");
        return "'" + strValue + "'";
    }

    /**
     * 构建排序字段的ORDER BY语句
     * @param sortFields 排序字段列表
     * @param isNext true=下一批(反序取第一条), false=上一批(正序取第一条)
     */
    private String buildSortFieldOrderBy(List<Map<String, Object>> sortFields, boolean isNext) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ORDER BY ");

        for (int i = 0; i < sortFields.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Map<String, Object> field = sortFields.get(i);
            String fieldName = (String) field.get("fieldName");
            String sortType = (String) field.get("sortType");

            // 下一批：反转排序方向（取第一条即最近的一条）
            // 上一批：保持原排序方向
            String orderDir = isNext ? ("asc".equalsIgnoreCase(sortType) ? "desc" : "asc") : sortType;
            sb.append(fieldName).append(" ").append(orderDir);
        }

        return sb.toString();
    }
}
