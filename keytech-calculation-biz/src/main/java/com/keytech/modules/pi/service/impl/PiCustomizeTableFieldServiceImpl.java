package com.keytech.modules.pi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.kpi.util.StringHandleUtils;
import com.keytech.modules.kpi.util.Tools;
import com.keytech.modules.pi.common.CommonConstants;
import com.keytech.modules.pi.common.PiCommonConstants;
import com.keytech.modules.common.DataBaseTypeUtil;
import com.keytech.modules.pi.dto.*;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiCustomizeTableFieldMapper;
import com.keytech.modules.pi.mapper.PiCustomizeTableMapper;
import com.keytech.modules.pi.service.*;
import com.keytech.modules.pi.util.ConditionParser;
import com.keytech.modules.pi.util.CustomizeTableUtil;
import com.keytech.modules.pi.util.SqlInjectionUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * @Description: pi自定义报表字段
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
@Service
@Slf4j
public class PiCustomizeTableFieldServiceImpl extends BaseServiceImpl<PiCustomizeTableFieldMapper, PiCustomizeTableField> implements IPiCustomizeTableFieldService {

    private static final Logger logger = LoggerFactory.getLogger(PiCustomizeTableFieldServiceImpl.class);
    @Resource
    private PiCustomizeTableMapper piCustomizeTableMapper;
    @Resource
    private PiCustomizeTableFieldMapper piCustomizeTableFieldMapper;

    @Resource
    private IPiCalculateSignalService iPiCalculateSignalService;
    @Resource
    private IPiRetrievalStepService iPiRetrievalStepService;
    @Resource
    private IPiComputationModelService iPiComputationModelService;
    @Resource
    private IPiComputationMonitorModelService iPiComputationMonitorModelService;
    @Resource
    private IPiRetrievalSignalService iPiRetrievalSignalService;
    @Resource
    private IPiCustomizeTableService ipiCustomizeTableService;
    @Resource
    private IPiAllocationTableFieldService piAllocationTableFieldService;
    @Resource
    private IPiTaskParameterService piTaskParameterService;
    @Resource
    private IPiAdapterAlgorithmService iPiAdapterAlgorithmService;
    @Resource
    private IPiStandardManageService iPiPiStandardManageService;
    @Resource
    private IPiStandardMaintenanceService iPiStandardMaintenanceService;
    @Resource
    private IPiFieldMergeService iPiFieldMergeService;
    @Resource
    private IPiStandardDataService iPiStandardDataService;

    @Resource
    private IPiDfAlgorithmBaseinfoService piDfAlgorithmBaseinfoService;

    private static String IDX_NAME = "idx_tenant_time_query";
    /**
     * 1.存储字段
     * 2.表中添加字段
     * @param piCustomizeTableField
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> saveField(PiCustomizeTableField piCustomizeTableField) {
        //获取表对应字段数量
        List<PiCustomizeTableField> list = piCustomizeTableFieldMapper.getFieldByTableId(piCustomizeTableField);
        int num = list.size();
        //生成指定字段名称(已有数量+1) field_0001
        piCustomizeTableField.setFieldName(String.format("field_%04d", (num+1)));

        //获取表名称
        PiCustomizeTable customizeTable= piCustomizeTableMapper.selectById(piCustomizeTableField.getTableId());

        if(null != customizeTable){
            //表名
            String tableName = customizeTable.getTableName();
            String subtables = customizeTable.getSubtable();
            //判断使用主表还是使用子表
            //视图
            String view = customizeTable.getViewName();
            //是否存在子表
            boolean isAttached = false;
            if(StrUtil.isNotBlank(view)){
                String[] subtableStr = subtables.split(",");
                piCustomizeTableField.setTableName(subtableStr[subtableStr.length-1]);
                isAttached = true;
            }else{
                piCustomizeTableField.setTableName(customizeTable.getTableName());
            }
            //获取类型
            if(DbType.POSTGRE_SQL == DataBaseTypeUtil.getDbType()){
                piCustomizeTableField.setFieldType(CustomizeTableUtil.getFieldTypePg(piCustomizeTableField.getType()));
            }else{
                piCustomizeTableField.setFieldType(CustomizeTableUtil.getFieldType(piCustomizeTableField.getType()));
            }
            //防止sql注入
            //SqlInjectionUtil.filterContent(piCustomizeTableField.getFieldName());
            SqlInjectionUtil.filterContent(piCustomizeTableField.getFieldLength()+"");
            if("1".equals(piCustomizeTableField.getType())){
                if(null == piCustomizeTableField.getFieldLength() || 0 == piCustomizeTableField.getFieldLength()){
                    //判断是否填写长度，默认给50
                    piCustomizeTableField.setFieldLength(50);
                } else if (500 < piCustomizeTableField.getFieldLength()){
                    //长度大于500，设置为500
                    piCustomizeTableField.setFieldLength(500);
                }
            }
            try {
                //表添加字段
                piCustomizeTableFieldMapper.addTableField(piCustomizeTableField);
                //存在记录
                piCustomizeTableField.setSubtable(piCustomizeTableField.getTableName());
                piCustomizeTableField.setTableName(tableName);
                save(piCustomizeTableField);

                //视图更新
                if(isAttached){
                    String viewName = tableName+"_view";
                    List<String> tableList = new ArrayList<>();
                    String[] subtableStr = subtables.split(",");
                    for (String table : subtableStr) {
                        tableList.add(table);
                    }
                    // 表集合 表名 a,表名1 b
                    StringBuffer tableBuff = new StringBuffer();
                    // 条件(a.id = b.main_id_1 and a.id = c.main_id_2)
                    StringBuffer conditionBuff = new StringBuffer();

                    for (int i = 0; i < tableList.size(); i++) {
                        char letter = Tools.getLetter(i);
                        String tName = tableList.get(i);
                        // 拼接表名和字母
                        tableBuff.append(tName).append(' ').append(letter);
                        //拼接条件
                        if(0 < i){
                            conditionBuff.append(" a.id = ").append(letter).append('.')
                               .append("main_id_").append(StringHandleUtils.getLastNumber(tName));
                        }
                        // 添加分隔符(如果不是最后一个元素)
                        if (i < tableList.size() - 1) {
                            tableBuff.append(",");
                            if(0 < i){conditionBuff.append(" AND ");}
                        }
                    }
                    //更新视图
                    piCustomizeTableFieldMapper.view(viewName,tableBuff.toString(),conditionBuff.toString());
                }

            } catch (Exception e) {
                //当捕获到 SQLSyntaxErrorException 且错误信息表明行大小超过 MySQL 限制
                if (e.getMessage() != null && (e.getMessage().contains("Row size too large") || e.getMessage().contains("maximum row size"))) {
                    //e.printStackTrace();
                    logger.error("字段总长度大于数据库限定长度");
                    //扩展表
                    createExtendTable(piCustomizeTableField,customizeTable);
                } else {
                    // 处理其他SQL语法错误
                    e.printStackTrace();
                }
            }
            //更新索引,排查扩展表(唯一约束)
            if(!isAttached && PiCommonConstants.ONE == piCustomizeTableField.getIsUnique()){
                updateIndex(piCustomizeTableField);
            }
            return Result.OK("保存成功!");
        }else{
            return Result.error("表不存在!");
        }
    }

    /**
     * 更新唯一约束索引
     * @param piCustomizeTableField
     */
    private void updateIndex(PiCustomizeTableField piCustomizeTableField) {
        //获取唯一约束集合
        LambdaQueryWrapper<PiCustomizeTableField> queryWrapper = new LambdaQueryWrapper<PiCustomizeTableField>();
        queryWrapper.eq(PiCustomizeTableField::getTableId,piCustomizeTableField.getTableId());
        queryWrapper.eq(PiCustomizeTableField::getIsUnique,PiCommonConstants.ONE);
        List<PiCustomizeTableField> fieldList = list(queryWrapper);
        //获取字段集合fieldName 逗号拼接
        StringBuffer fieldNameStBu = new StringBuffer();
        fieldNameStBu.append(CommonConstants.TENANT_ID).append( ",");
        for (PiCustomizeTableField field : fieldList) {
            fieldNameStBu.append(field.getFieldName()).append(",");
        }
        fieldNameStBu.deleteCharAt(fieldNameStBu.length()-1);

        //索引名称
        String idxName = IDX_NAME;
        if (DataBaseTypeUtil.getDbType() == DbType.POSTGRE_SQL) {
            idxName += "_" + piCustomizeTableField.getTableName();
        }
        // 判断索引是否存在
        int index = piCustomizeTableFieldMapper.selectIndex(idxName, piCustomizeTableField.getTableName());
        if(index > PiCommonConstants.ZERO){
            //删除索引
            piCustomizeTableFieldMapper.deleteIndex(idxName, piCustomizeTableField.getTableName());
        }
        //创建索引
        try {
            piCustomizeTableFieldMapper.createIndex(idxName, piCustomizeTableField.getTableName(), fieldNameStBu.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 创建子表(扩展表)
     *
     */
    public void createExtendTable(PiCustomizeTableField customizeTableField, PiCustomizeTable customizeTable){
        //主表名称
        String tableName = customizeTable.getTableName();
        //储存主表和所有子表
        List<String> tableList = new ArrayList<>();
//        tableList.add(tableName);
        //获取子表名称 主表名称+"_"+子表序号
        //子表序号
        int tableNum = 1;
        String subtables = customizeTable.getSubtable();
        if(StrUtil.isNotBlank(subtables)){
            String[] subtableStr = subtables.split(",");
            for (String table : subtableStr) {
                tableList.add(table);
            }
//            tableNum = subtableStr.length + 2;
            tableNum = subtableStr.length;
            customizeTableField.setTableName(tableName+"_" + tableNum);
            tableList.add(customizeTableField.getTableName());
        }else{
            //首个子表
            customizeTableField.setTableName(tableName+"_" + tableNum);
            tableList.add(customizeTableField.getTableName());
        }

        String viewName = tableName+"_view";
//        String viewTables = "";
//        String viewConditions = "";
        // 表集合 表名 a,表名1 b
        StringBuffer tableBuff = new StringBuffer();
        // 条件(a.id = b.main_id_1 and a.id = c.main_id_2)
        StringBuffer conditionBuff = new StringBuffer();

        for (int i = 0; i < tableList.size(); i++) {
            char letter = Tools.getLetter(i);
            String tName = tableList.get(i);
            // 拼接表名和字母
            tableBuff.append(tName).append(' ').append(letter);

            //拼接条件
            if(0 < i){
                conditionBuff.append(" a.id = ").append(letter).append('.')
                        .append("main_id_").append(StringHandleUtils.getLastNumber(tName));
            }
            // 添加分隔符(如果不是最后一个元素)
            if (i < tableList.size() - 1) {
                tableBuff.append(",");

                if(0 < i){
                    conditionBuff.append(" AND ");
                }
            }
        }

        try {
            //创建子表(扩展表)
            PiCustomizeTable piCustomizeTable = new PiCustomizeTable();
            piCustomizeTable.setTableName(customizeTableField.getTableName());
            piCustomizeTable.setField("main_id_"+tableNum);
            piCustomizeTableMapper.createExtendTable(piCustomizeTable);
            //更新主表(视图名称,子表集合)
            customizeTable.setViewName(viewName);
            customizeTable.setSubtable(String.join(",", tableList));
            piCustomizeTableMapper.updateById(customizeTable);
            //----存在记录
            //----表添加字段
            //添加字段
            piCustomizeTableFieldMapper.addTableField(customizeTableField);

            //创建/更新视图
            piCustomizeTableFieldMapper.view(viewName,tableBuff.toString(),conditionBuff.toString());
            //存储字段记录
            customizeTableField.setTableName(tableName);
            customizeTableField.setSubtable(piCustomizeTable.getTableName());
            save(customizeTableField);
            //将主表数据id同步到子表中(main_id)
            piCustomizeTableFieldMapper.synchronizeData(customizeTableField.getSubtable(), piCustomizeTable.getField(), tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 算法存储
     * @param algorithmDataDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void algorithmAdd(AlgorithmDataDto algorithmDataDto) {
        //piCustomizeTableFieldMapper.algorithmAdd();
        //储存-计算信号
        PiCalculateSignal piCalculateSignal = algorithmDataDto.getCalculateSignal();
        piCalculateSignal.setDelFlag(0);
        iPiCalculateSignalService.saveOrUpdate(piCalculateSignal);

        //存储-取数信号
        String retrievalSignal = algorithmDataDto.getRetrievalSignal();
        List<PiRetrievalSignal> retrievalSignalList = new ArrayList<>();
        if(StrUtil.isNotBlank(retrievalSignal)){
            JSONArray jsonArray = JSONUtil.parseArray(retrievalSignal);
            Double num = 1.0;
            for (Object object : jsonArray) {
                String identifier = IdUtil.simpleUUID();
                JSONArray arr = (JSONArray)object;
                for (Object obj : arr) {
                    PiRetrievalSignal piRetrievalSignal = BeanUtil.copyProperties(obj, PiRetrievalSignal.class);
                    piRetrievalSignal.setCalculateId(piCalculateSignal.getId());
                    piRetrievalSignal.setIdentifier(identifier);
                    piRetrievalSignal.setSort(num);
                    piRetrievalSignal.setDelFlag(0);
                    retrievalSignalList.add(piRetrievalSignal);
                }
                num++;
            }
        }
        if(null != retrievalSignalList){
            //获取之前存储的取数信号
            LambdaQueryWrapper<PiRetrievalSignal> queryWrapper = new LambdaQueryWrapper<PiRetrievalSignal>();
            queryWrapper.eq(PiRetrievalSignal::getCalculateId,piCalculateSignal.getId());
            List<PiRetrievalSignal> retrievalSignalOldList = iPiRetrievalSignalService.list(queryWrapper);
            //获取删除
            List<PiRetrievalSignal> listDel = new ArrayList<>();
            for (PiRetrievalSignal piRetrievalSignalOld : retrievalSignalOldList) {
                boolean isDel = true;
                for (PiRetrievalSignal piRetrievalSignalNew : retrievalSignalList) {
                    if(StrUtil.isNotBlank(piRetrievalSignalNew.getId())) {
                        if (piRetrievalSignalNew.getId().equals(piRetrievalSignalOld.getId())) {
                            isDel = false;
                            break;
                        }
                    }
                }
                if(isDel){
                    listDel.add(piRetrievalSignalOld);
                }
            }
            //删除
            iPiRetrievalSignalService.removeBatchByIds(listDel);
            //新增/修改
            iPiRetrievalSignalService.saveOrUpdateBatch(retrievalSignalList);
        }

        //存储-取数步骤
        PiRetrievalStep retrievalStepStart = algorithmDataDto.getRetrievalStepStart();
        PiRetrievalStep retrievalStepEnd = algorithmDataDto.getRetrievalStepEnd();
        retrievalStepStart.setCalculateId(piCalculateSignal.getId());
        retrievalStepStart.setDelFlag(0);
        retrievalStepEnd.setCalculateId(piCalculateSignal.getId());
        retrievalStepEnd.setDelFlag(0);
        if(StrUtil.isNotBlank(retrievalStepStart.getFrequency())){
            retrievalStepEnd.setFrequency(retrievalStepStart.getFrequency());
        }
        if(StrUtil.isNotBlank(retrievalStepStart.getIsSlicing())){
            retrievalStepEnd.setIsSlicing(retrievalStepStart.getIsSlicing());
        }
        iPiRetrievalStepService.saveOrUpdate(retrievalStepStart);
        iPiRetrievalStepService.saveOrUpdate(retrievalStepEnd);

        //存储-计算模型
        PiComputationModel computationModel = algorithmDataDto.getComputationModel();
        computationModel.setCalculateId(piCalculateSignal.getId());
        computationModel.setDelFlag(0);
        iPiComputationModelService.saveOrUpdate(computationModel);

        //存储-计算模型(监控取值)
        if("2".equals(piCalculateSignal.getStepType())){
            List<PiComputationMonitorModel> computationMonitorModelList = algorithmDataDto.getComputationMonitorModelList();

            //获取之前存储的取数信号
            LambdaQueryWrapper<PiComputationMonitorModel> queryWrapper = new LambdaQueryWrapper<PiComputationMonitorModel>();
            queryWrapper.eq(PiComputationMonitorModel::getCalculateId,piCalculateSignal.getId());
            List<PiComputationMonitorModel> monitorModelOldList = iPiComputationMonitorModelService.list(queryWrapper);
            //获取删除
            List<PiComputationMonitorModel> listDel = new ArrayList<>();
            for (PiComputationMonitorModel monitorModelOld : monitorModelOldList) {
                boolean isDel = true;
                for (PiComputationMonitorModel monitorModelNew : computationMonitorModelList) {
                    if(StrUtil.isNotBlank(monitorModelNew.getId())) {
                        if (monitorModelNew.getId().equals(monitorModelOld.getId())) {
                            isDel = false;
                            break;
                        }
                    }
                }
                if(isDel){
                    listDel.add(monitorModelOld);
                }
            }
            //删除
            iPiComputationMonitorModelService.removeBatchByIds(listDel);

            //序号
            int sort = 1;
            //新增/修改
            for (PiComputationMonitorModel computationMonitorModel : computationMonitorModelList) {
                computationMonitorModel.setDelFlag(0);
                computationMonitorModel.setCalculateId(piCalculateSignal.getId());
                computationMonitorModel.setSort(sort++);
            }
            iPiComputationMonitorModelService.saveOrUpdateBatch(computationMonitorModelList);
        }
    }

    @Override
    public List<SinicizedDictDto> getDictionary() {
        return piCustomizeTableFieldMapper.getDictionary();
    }


    /**
     * 查询报表数据
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Override
    public Map<String, Object> queryData(Integer pageNo, Integer pageSize, HttpServletRequest req, PiCustomizeTableField piCustomizeTableField) {
        Map<String,Object> mapR = new HashMap<>();
        List<Map<String,Object>> list = new ArrayList<>();
        int tatol = 0;
        //自定义报表id
        String tableId = piCustomizeTableField.getTableId();
        if(StrUtil.isNotBlank(tableId) && StrUtil.isNotBlank(tableId)){
            //获取自定义报表
            PiCustomizeTable piCustomizeTable = ipiCustomizeTableService.getById(tableId);
            //获取查询条件(分配表中)
            LambdaQueryWrapper<PiCustomizeTableField> queryWrapper = new LambdaQueryWrapper<PiCustomizeTableField>();
            queryWrapper.eq(PiCustomizeTableField::getTableId,tableId);
//            queryWrapper.eq(PiCustomizeTableField::getIsQuery,"1");
            queryWrapper.eq(PiCustomizeTableField::getIsList,"1");
            List<PiCustomizeTableField> fieldList = list(queryWrapper);

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

            //查询条件
            StringBuffer conditionBuff = new StringBuffer();
            //返回字段
            StringBuffer fieldBuff = new StringBuffer();
            fieldBuff.append(" id ");

            //排序集合
            StringBuffer sortBuff = new StringBuffer();

            // 数据过滤条件[条件传入]
            if(StrUtil.isNotBlank(piCustomizeTableField.getOtherConditionJson()) && !"[]".equals(piCustomizeTableField.getOtherConditionJson())){
                conditionBuff.append(" and ").append(ConditionParser.parseCondition(piCustomizeTableField.getOtherConditionJson()));
            }


            //遍历字段
            for (PiCustomizeTableField tableField : fieldList) {
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
                        }
                    }else{
                        conditionBuff.append(" and ").append(tableField.getFieldName()).append(" like '%").append(paramMap.get(tableField.getFieldName())+"").append("%' ");
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
            //排序为空设置默认值
            if(null != sortBuff && sortBuff.length() == 0){
                sortBuff.append(" order by  create_time desc ");
            }

            //查询字段
            paramMap.put("query",fieldBuff.toString());
            //查询条件
            paramMap.put("condition",conditionBuff.toString());
            //查询表
            String tableName = piCustomizeTable.getTableName();
            //判断查视图,还是表
            if(StrUtil.isNotBlank(piCustomizeTable.getViewName())){
                //视图
                tableName = piCustomizeTable.getViewName();
            }
            paramMap.put("tableName", tableName);
            //排序(默认创建时间)
            paramMap.put("sort",sortBuff.toString());


            //查询总数
            tatol = piAllocationTableFieldService.getTableDataCount(paramMap);
            if(tatol > 0){
                //分页查询
                paramMap.put("pageNo",(pageNo-1) * pageSize);
                paramMap.put("pageSize", pageSize);
                list = piAllocationTableFieldService.getTableData(paramMap);

                //获取红黄绿标准数据结果
                LambdaQueryWrapper<PiStandardData> standardDataQueryWrapper = new LambdaQueryWrapper<>();
                standardDataQueryWrapper.eq(PiStandardData::getTableName, piCustomizeTable.getTableName());
                List<PiStandardData> piStandardDataList = iPiStandardDataService.list(standardDataQueryWrapper);
                for (Map<String, Object> map : list) {
                    //比对获取标准数据红黄绿计算结果
                    String standardJson = "";
                    for (PiStandardData piStandardData : piStandardDataList) {
                        if (MapUtil.getStr(map, "id").equals(piStandardData.getDataId())) {
                            standardJson = piStandardData.getStandardJson();
                            break;
                        }
                    }
                    map.put("piStandardData", standardJson);
                }
            }
        }
        mapR.put("records",list);
        mapR.put("total",tatol);  //临时
        return mapR;
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
            PiCustomizeTable piCustomizeTable = ipiCustomizeTableService.getById(map.get("tableId")+"");
            if(null != piCustomizeTable){
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
                tableSaveDto.setTable(piCustomizeTable.getTableName());
                tableSaveDto.setConditionList(conditionList);
                tableSaveDto.setFieldList(fieldList);
                //更新
                String re = ipiCustomizeTableService.tableSave(tableSaveDto);
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
     * 获取自定义报表
     * 除定时任务配置过的字段
     * @param piCustomizeTableField
     * @return
     */
    @Override
    public List<PiCustomizeTableField> fieldByTableIdData(PiCustomizeTableField piCustomizeTableField) {
        List<PiCustomizeTableField> reList = new ArrayList<>();
        //获取报表下的所有字段
        LambdaQueryWrapper<PiCustomizeTableField> queryWrapper = new LambdaQueryWrapper<PiCustomizeTableField>();
        queryWrapper.eq(PiCustomizeTableField::getTableId,piCustomizeTableField.getTableId());
        queryWrapper.eq(PiCustomizeTableField::getIsUnique,piCustomizeTableField.getIsUnique());
        List<PiCustomizeTableField> list = list(queryWrapper);

        //排除定时任务分配的字段
        //获取定时任务 (piCustomizeTableField.getId()-定时任务id)
        LambdaQueryWrapper<PiTaskParameter> taskQueryWrapper = new LambdaQueryWrapper<PiTaskParameter>();
        taskQueryWrapper.eq(PiTaskParameter::getTableId,piCustomizeTableField.getTableId());
        taskQueryWrapper.eq(PiTaskParameter::getTaskId,piCustomizeTableField.getTaskId());
        List<PiTaskParameter> taskList = piTaskParameterService.list(taskQueryWrapper);

        //将field转化成set
        Set<String> fieldSet = taskList.stream()
                .filter(task->!task.getId().equals(piCustomizeTableField.getId()))
                .map(PiTaskParameter::getFields)
                .flatMap(fields -> Arrays.stream(fields.split(",")))
                .collect(Collectors.toSet());

        //获取算法主表(基础算法)
        LambdaQueryWrapper<PiCalculateSignal> calculateSignalqueryWrapper = new LambdaQueryWrapper<PiCalculateSignal>();
        calculateSignalqueryWrapper.eq(PiCalculateSignal::getPiTable,piCustomizeTableField.getTableName());
        calculateSignalqueryWrapper.eq(PiCalculateSignal::getDelFlag,0);
        List<PiCalculateSignal> calculateSignalList = iPiCalculateSignalService.list(calculateSignalqueryWrapper);

        //获取Adapter算法
        LambdaQueryWrapper<PiAdapterAlgorithm> adapterqueryWrapper = new LambdaQueryWrapper<PiAdapterAlgorithm>();
        adapterqueryWrapper.in(PiAdapterAlgorithm::getPiTable, piCustomizeTableField.getTableName());
        adapterqueryWrapper.eq(PiAdapterAlgorithm::getDelFlag,0);
        List<PiAdapterAlgorithm> adapterAlgorithmList = iPiAdapterAlgorithmService.list(adapterqueryWrapper);

        //合并到基础算法
        for (PiAdapterAlgorithm piAdapterAlgorithm : adapterAlgorithmList) {
            PiCalculateSignal piCalculateSignal = new PiCalculateSignal();
            piCalculateSignal.setPiTable(piAdapterAlgorithm.getPiTable());
            piCalculateSignal.setPiCode(piAdapterAlgorithm.getPiCode());
            calculateSignalList.add(piCalculateSignal);
        }

        Set<String> signalSet = calculateSignalList.stream()
                .map(PiCalculateSignal::getPiCode)
                .collect(Collectors.toSet());
        //过滤出没有配置定时任务,且配置算法
        reList = list.stream()
                .filter(entity ->!fieldSet.contains(entity.getFieldName()))
                .filter(entity ->signalSet.contains(entity.getFieldName()))
                .collect(Collectors.toList());

        return reList;
    }

    /**
     * 算法复制
     * @param piCustomizeTableField
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> algorithmCopy(PiCustomizeTableField piCustomizeTableField) {
        // 判断当前使用算法 currentAlgorithm
        if("InfluxDB".equals(piCustomizeTableField.getCurrentAlgorithm())){
            // InfluxDB 算法
            copyInfluxDb(piCustomizeTableField);
        }else{
            // Adapter算法/Ra算法复制
            iPiAdapterAlgorithmService.copyAdapterRaDb(piCustomizeTableField);
        }
        return null;
    }

    //InfluxDB算法复制
    public void copyInfluxDb(PiCustomizeTableField piCustomizeTableField){
        //计算信号
        LambdaQueryWrapper<PiCalculateSignal> queryWrapper = new LambdaQueryWrapper<PiCalculateSignal>();
        queryWrapper.eq(PiCalculateSignal::getPiTable,piCustomizeTableField.getTableName());
        queryWrapper.eq(PiCalculateSignal::getPiCode,piCustomizeTableField.getFieldName());
        queryWrapper.orderByAsc(PiCalculateSignal::getCreateTime).orderByAsc(PiCalculateSignal::getId);
        queryWrapper.eq(PiCalculateSignal::getDelFlag,0);
        List<PiCalculateSignal> list = iPiCalculateSignalService.list(queryWrapper);

        if(null != list && list.size() > 0){
            for (PiCalculateSignal piCalculateSignal : list) {
                //历史计算信号id
                String calculateId = piCalculateSignal.getId();
                //计算信号-存储
                piCalculateSignal.setId(null);
                piCalculateSignal.setPiCode(piCustomizeTableField.getTargetField());
                //pi名称(目标)
                piCalculateSignal.setPiName(piCustomizeTableField.getTargetFieldName());
                iPiCalculateSignalService.save(piCalculateSignal);

                //取数信号
                LambdaQueryWrapper<PiRetrievalSignal> retrievalSignalWrapper = Wrappers.lambdaQuery(PiRetrievalSignal.class);
                retrievalSignalWrapper.eq(PiRetrievalSignal::getCalculateId,calculateId);
                retrievalSignalWrapper.orderByAsc(PiRetrievalSignal::getSort).orderByAsc(PiRetrievalSignal::getId);
                retrievalSignalWrapper.eq(PiRetrievalSignal::getDelFlag,0);
                List<PiRetrievalSignal> retrievalSignalList = iPiRetrievalSignalService.list(retrievalSignalWrapper);
                //存储取数信号
                if(null != retrievalSignalList && retrievalSignalList.size() > 0){
                    for (PiRetrievalSignal piRetrievalSignal : retrievalSignalList) {
                        piRetrievalSignal.setId(null);
                        piRetrievalSignal.setCalculateId(piCalculateSignal.getId());
                        iPiRetrievalSignalService.save(piRetrievalSignal);
                    }
                }


                //取数步骤
                LambdaQueryWrapper<PiRetrievalStep> retrievalStepWrapper = Wrappers.lambdaQuery(PiRetrievalStep.class);
                retrievalStepWrapper.eq(PiRetrievalStep::getCalculateId,calculateId);
                retrievalStepWrapper.eq(PiRetrievalStep::getDelFlag,0);
                List<PiRetrievalStep> retrievalStepList = iPiRetrievalStepService.list(retrievalStepWrapper);
                if(null != retrievalStepList && retrievalStepList.size() > 0){
                    for (PiRetrievalStep piRetrievalStep : retrievalStepList) {
                        piRetrievalStep.setId(null);
                        piRetrievalStep.setCalculateId(piCalculateSignal.getId());
                        iPiRetrievalStepService.save(piRetrievalStep);
                    }
                }


                //计算模型
                LambdaQueryWrapper<PiComputationModel> computationModelWrapper = Wrappers.lambdaQuery(PiComputationModel.class);
                computationModelWrapper.eq(PiComputationModel::getCalculateId,calculateId);
                computationModelWrapper.eq(PiComputationModel::getDelFlag,0);
                List<PiComputationModel> computationModelList = iPiComputationModelService.list(computationModelWrapper);
                if(null != computationModelList && computationModelList.size() > 0){
                    for (PiComputationModel piComputationModel : computationModelList) {
                        piComputationModel.setId(null);
                        piComputationModel.setCalculateId(piCalculateSignal.getId());
                        iPiComputationModelService.save(piComputationModel);
                    }
                }

                //计算模型(监控取值)
                LambdaQueryWrapper<PiComputationMonitorModel> computationMonitorModelWrapper = Wrappers.lambdaQuery(PiComputationMonitorModel.class);
                computationMonitorModelWrapper.eq(PiComputationMonitorModel::getCalculateId,calculateId);
                computationMonitorModelWrapper.eq(PiComputationMonitorModel::getDelFlag,0);
                List<PiComputationMonitorModel> computationMonitorModelList = iPiComputationMonitorModelService.list(computationMonitorModelWrapper);
                if(null != computationMonitorModelList && computationMonitorModelList.size() > 0){
                    for (PiComputationMonitorModel piComputationMonitorModel : computationMonitorModelList) {
                        piComputationMonitorModel.setId(null);
                        piComputationMonitorModel.setCalculateId(piCalculateSignal.getId());
                        iPiComputationMonitorModelService.save(piComputationMonitorModel);
                    }
                }
            }
            //更新自定义报表字段目标表的当前使用类型
            LambdaUpdateWrapper<PiCustomizeTableField> updateWrapper = Wrappers.lambdaUpdate(PiCustomizeTableField.class);
            updateWrapper.eq(PiCustomizeTableField::getTableName, piCustomizeTableField.getTableName());
            updateWrapper.eq(PiCustomizeTableField::getFieldName, piCustomizeTableField.getTargetField());
            updateWrapper.eq(PiCustomizeTableField::getDelFlag, 0);
            updateWrapper.set(PiCustomizeTableField::getCurrentAlgorithm, piCustomizeTableField.getCurrentAlgorithm());
            updateWrapper.set(PiCustomizeTableField::getUpdateTime, new Date());
            update(updateWrapper);
        }
    }


    /**
     * 获取排序
     * @param tableId
     * @return
     */
    @Override
    public List<PiCustomizeTableField> getSort(String tableId) {
        //获取序号最新的一条数据
        LambdaQueryWrapper<PiCustomizeTableField> computationModelWrapper = Wrappers.lambdaQuery(PiCustomizeTableField.class);
        computationModelWrapper.eq(PiCustomizeTableField::getTableId,tableId);
        computationModelWrapper.isNotNull(PiCustomizeTableField::getSort);
        computationModelWrapper.orderByDesc(PiCustomizeTableField::getSort);
        computationModelWrapper.last("LIMIT " + 1);
        List<PiCustomizeTableField> list = list(computationModelWrapper);
        if(null != list && list.size() > 0){
            PiCustomizeTableField piCustomizeTableField = list.get(0);
            Double sort = piCustomizeTableField.getSort();
            if(null != sort){
                //加一向下取整
                piCustomizeTableField.setSort(Math.floor(sort+1));
            }
        }else{
            list = new ArrayList<PiCustomizeTableField>();
            list.add(new PiCustomizeTableField().setSort(1.0));
        }
        return list;
    }

    /**
     * 判断是否配置算法
     * @param list
     * @return
     */
    @Override
    public Page<PiCustomizeTableField> statisticsAlgorithm(Page<PiCustomizeTableField> list) {
        if(null != list && list.getRecords().size() > 0){
            List<PiCustomizeTableField> fieldList = list.getRecords();
            String tableName = fieldList.get(0).getTableName();
            //获取算法主表
            LambdaQueryWrapper<PiCalculateSignal> queryWrapper = new LambdaQueryWrapper<PiCalculateSignal>();
            queryWrapper.eq(PiCalculateSignal::getPiTable,tableName);
            queryWrapper.eq(PiCalculateSignal::getDelFlag,0);
            List<PiCalculateSignal> calculateSignalList = iPiCalculateSignalService.list(queryWrapper);
            //list转map 去除重复的piCode
            BinaryOperator<PiCalculateSignal> mergeFunction = (existing, replacement) -> existing; // 保留第一个元素
            Map<String, PiCalculateSignal> signalMap = calculateSignalList.stream()
                    .collect(Collectors.toMap(PiCalculateSignal::getPiCode, signal -> signal, mergeFunction));

            //获取Adapter算法
            LambdaQueryWrapper<PiAdapterAlgorithm> adapterqueryWrapper = new LambdaQueryWrapper<PiAdapterAlgorithm>();
            adapterqueryWrapper.in(PiAdapterAlgorithm::getPiTable,tableName);
            adapterqueryWrapper.eq(PiAdapterAlgorithm::getDelFlag,0);
            List<PiAdapterAlgorithm> adapterAlgorithmList = iPiAdapterAlgorithmService.list(adapterqueryWrapper);
            Map<String, String> adapterMap = adapterAlgorithmList.stream()
                    .collect(Collectors.toMap(
                            PiAdapterAlgorithm::getPiCode,
                            PiAdapterAlgorithm::getPiCode,
                            (existing, replacement) -> existing
                    ));

            //DF算法
            List<PiDfAlgorithmBaseinfo> piDfAlgorithmBaseinfoList = piDfAlgorithmBaseinfoService.lambdaQuery()
                    .eq(PiDfAlgorithmBaseinfo::getPiTableName,tableName)
                    .list();
            Map<String, String> dfMap = piDfAlgorithmBaseinfoList.stream()
                    .collect(Collectors.toMap(
                            PiDfAlgorithmBaseinfo::getPiFieldName,
                            PiDfAlgorithmBaseinfo::getPiFieldName,
                            (existing, replacement) -> existing
                    ));


            //判断是否字段是否配置算法
            for (PiCustomizeTableField piCustomizeTableField : fieldList) {
                String isAlgorithm = "0";
                if(
                    signalMap.containsKey(piCustomizeTableField.getFieldName())
                    || adapterMap.containsKey(piCustomizeTableField.getFieldName())
                    || dfMap.containsKey(piCustomizeTableField.getFieldName())
                ){
                    isAlgorithm = "1";
                }
                piCustomizeTableField.setIsAlgorithm(isAlgorithm);
            }
        }
        return list;
    }

    /**
     * 删除字段, 同时删除相关算法
     * @param ids
     */
    @Override
    public void deleteBatchData(List<String> ids) {
        if(null != ids && ids.size() > 0){
            String id = ids.get(0);
            PiCustomizeTableField piCustomizeTableField = getById(id);
            //通过id集合获取字段集合
            List<PiCustomizeTableField> listField = listByIds(ids);
            List<String> fieldNameList = listField.stream()
                    .map(PiCustomizeTableField::getFieldName)
                    .collect(Collectors.toList());

            //获取算法主表集合
            LambdaQueryWrapper<PiCalculateSignal> queryWrapper = new LambdaQueryWrapper<PiCalculateSignal>();
            queryWrapper.eq(PiCalculateSignal::getPiTable,piCustomizeTableField.getTableName());
            queryWrapper.in(PiCalculateSignal::getPiCode,fieldNameList);
            queryWrapper.eq(PiCalculateSignal::getDelFlag,0);
            List<PiCalculateSignal> calculateSignalList = iPiCalculateSignalService.list(queryWrapper);

            List<String> calculateIdList = calculateSignalList.stream()
                    .map(PiCalculateSignal::getId)
                    .collect(Collectors.toList());

            //删除算法
            if(null != calculateIdList && calculateIdList.size() > 0){
              iPiCalculateSignalService.deleteDataBatch(calculateIdList);
            }

            //获取Adapter算法
            LambdaQueryWrapper<PiAdapterAlgorithm> adapterqueryWrapper = new LambdaQueryWrapper<PiAdapterAlgorithm>();
            adapterqueryWrapper.in(PiAdapterAlgorithm::getPiTable,piCustomizeTableField.getTableName());
            adapterqueryWrapper.in(PiAdapterAlgorithm::getPiCode,fieldNameList);
            List<PiAdapterAlgorithm> adapterAlgorithmList = iPiAdapterAlgorithmService.list(adapterqueryWrapper);
            List<String> adapterIdList = adapterAlgorithmList.stream()
                    .map(PiAdapterAlgorithm::getId)
                    .collect(Collectors.toList());
            //删除Adapter算法
            if(null != adapterIdList && adapterIdList.size() > 0){
                iPiAdapterAlgorithmService.deleteDataBatch(adapterIdList);
            }

            //删除字段
            removeByIds(ids);
        }
    }


    /**
     * 根据更新表字段/同步PI字段
     * @param piCustomizeTableField 表字段数据对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateByData(PiCustomizeTableField piCustomizeTableField) {
        //判断PI是否配置PI间计算 去掉限制
//        if(piCustomizeTableField.getIsEdit().equals(PiCommonConstants.OPEN) &&
//                iPiCalculateSignalService.isPiOtherCalculation(piCustomizeTableField.getTableName(),piCustomizeTableField.getFieldName())) {
//            //存在PI间计算
//            return Result.error(PiCommonConstants.PI_CANNOT_EDIT);
//        }
        updateById(piCustomizeTableField);
        //同步PI报表字段
        PiAllocationTableField piAllocationTableField = new PiAllocationTableField();
        //字段中文名称
        piAllocationTableField.setName(piCustomizeTableField.getName());
        //是否唯一约束
        piAllocationTableField.setIsUnique(piCustomizeTableField.getIsUnique());
        //是否重点关注PI
        piAllocationTableField.setIsFocus(piCustomizeTableField.getIsFocus());
        //是否重点关注PI
        piAllocationTableField.setIsFactoryFocus(piCustomizeTableField.getIsFactoryFocus());
        //是否参与绿区率统计
        piAllocationTableField.setIsStatistics(piCustomizeTableField.getIsStatistics());
        //绿区率目标值(%)
        piAllocationTableField.setGreenTarget(piCustomizeTableField.getGreenTarget());
        //保留几位小数
        piAllocationTableField.setKeepDecimals(piCustomizeTableField.getKeepDecimals());
        //取值方式
        piAllocationTableField.setTakingValue(piCustomizeTableField.getTakingValue());
        //单位
        piAllocationTableField.setPiUnit(piCustomizeTableField.getPiUnit());
        //备注
        piAllocationTableField.setNotes(piCustomizeTableField.getNotes());
        //填报项组件
        piAllocationTableField.setEditWidgetType(piCustomizeTableField.getEditWidgetType());
        //组件字典配置
        piAllocationTableField.setDropdownData(piCustomizeTableField.getDropdownData());
        //是否自动PI
        piAllocationTableField.setAutomaticPi(piCustomizeTableField.getAutomaticPi());
        // 是否启用设定值
        piAllocationTableField.setIsValueEnabled(piCustomizeTableField.getIsValueEnabled());
        //设定值
        piAllocationTableField.setSetValue(piCustomizeTableField.getSetValue());
        //时间展示格式
        piAllocationTableField.setTimeFormat(piCustomizeTableField.getTimeFormat());
        //查询是否显示历史
        piAllocationTableField.setHistoryData(piCustomizeTableField.getHistoryData());

        UpdateWrapper<PiAllocationTableField> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(PiCommonConstants.TABLE_ID,piCustomizeTableField.getTableId());
        updateWrapper.eq(PiCommonConstants.FIELD_ID,piCustomizeTableField.getId());
        piAllocationTableFieldService.update(piAllocationTableField,updateWrapper);

        //更新索引,排查扩展表(唯一约束)
        if(PiCommonConstants.ONE == piCustomizeTableField.getIsUnique()){
            //过滤掉扩展表
            PiCustomizeTable customizeTable= piCustomizeTableMapper.selectById(piCustomizeTableField.getTableId());
            // 排除扩展表
            if(!StringUtils.contains(customizeTable.getSubtable(), ",")) {
                updateIndex(piCustomizeTableField);
            }
        }
        return Result.OK(PiCommonConstants.EDIT_SUCCESS);
    }


    /**
     * 开启/关闭配置算法的编辑状态
     * @param tableId
     * @param isEdit
     */
    @Override
    public void openOrCloseEdit(String tableId, String isEdit) {

        //获取分配的所有字段
        LambdaQueryWrapper<PiCustomizeTableField> fieldWrapper = new LambdaQueryWrapper<>();
        fieldWrapper.eq(PiCustomizeTableField::getTableId, tableId);
        fieldWrapper.eq(PiCustomizeTableField::getIsEdit,
                PiCommonConstants.OPEN.equals(isEdit)? PiCommonConstants.CLOSE :PiCommonConstants.OPEN);

        List<PiCustomizeTableField> list = list(fieldWrapper);
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
            List<PiCustomizeTableField> result = list.stream()
                    .filter(field -> calcKeys.contains(field.getTableName() + "|" + field.getFieldName()))
                    .peek(field -> field.setIsEdit(isEdit))  // 设置isEdit属性
                    .collect(Collectors.toList());

            //更新数据
            updateBatchById(result);

            List<String> idList = result.stream()
                    .map(PiCustomizeTableField::getId)
                    .collect(Collectors.toList());

            //同步分配表编辑状态
            LambdaQueryWrapper<PiAllocationTableField> allocationWrapper = new LambdaQueryWrapper<>();
            allocationWrapper.in(PiAllocationTableField::getFieldId, idList);
            allocationWrapper.eq(PiAllocationTableField::getIsEdit,
                    PiCommonConstants.OPEN.equals(isEdit)? PiCommonConstants.CLOSE :PiCommonConstants.OPEN);
            List<PiAllocationTableField> allocationList = piAllocationTableFieldService.list(allocationWrapper);

            List<PiAllocationTableField> allocationResult = allocationList.stream()
                    .peek(field -> field.setIsEdit(isEdit))  // 设置isEdit属性
                    .collect(Collectors.toList());
            piAllocationTableFieldService.updateBatchById(allocationResult);

        }

    }

    /**
     * 获取字段(标记是否配置标准)
     * @param piCustomizeTableField
     * @param parameterMap
     * @return
     */
    @Override
    public List<PiCustomizeTableField> getStandardFieldByTableId(PiCustomizeTableField piCustomizeTableField, Map<String, String[]> parameterMap) {
        //获取字段
        QueryWrapper<PiCustomizeTableField> queryWrapper = QueryGenerator.initQueryWrapper(piCustomizeTableField, parameterMap);
        queryWrapper.orderByDesc("is_unique").orderByAsc("sort");
        List<PiCustomizeTableField> list = list(queryWrapper);

        //获取标准管理
        LambdaQueryWrapper<PiStandardManage> standardWrapper = new LambdaQueryWrapper<>();
        standardWrapper.eq(PiStandardManage::getCustomizeId, piCustomizeTableField.getTableId());
        List<PiStandardManage> standardList = iPiPiStandardManageService.list(standardWrapper);
        //标准主键id集合
        List<String> standardIdList = standardList.stream()
                .map(PiStandardManage::getId)
                .collect(Collectors.toList());
        if(standardIdList != null && standardIdList.size() > 0){
            //获取标准表
            LambdaQueryWrapper<PiStandardMaintenance> standardTableWrapper = new LambdaQueryWrapper<>();
            standardTableWrapper.in(PiStandardMaintenance::getManageId, standardIdList);
            List<PiStandardMaintenance> standardMainList = iPiStandardMaintenanceService.list(standardTableWrapper);
            //获取字段名称 map集合
            Map<String, String> fieldNameMap = standardMainList.stream()
            .collect(Collectors.toMap(
                    PiStandardMaintenance::getFieldName,
                    PiStandardMaintenance::getFieldText,
                    (existing, replacement) -> existing  // 保留已存在的值
            ));
            //遍历字段 判断字段名称是否在标准中存在,存在修改是否配置标准(1)
            for (PiCustomizeTableField field : list) {
                if(fieldNameMap.containsKey(field.getFieldName())){
                    field.setIsStandard(PiCommonConstants.OPEN);
                }else {
                    field.setIsStandard(PiCommonConstants.CLOSE);
                }
            }
        }

        return list;
    }

    @Override
    public List<PiCustomizeTableField> getFieldByTableId(String tableId) {
        LambdaQueryWrapper<PiCustomizeTableField> wrapperDevice = new LambdaQueryWrapper<>();
        wrapperDevice.eq(PiCustomizeTableField::getTableId, tableId);
        wrapperDevice.orderByAsc(PiCustomizeTableField::getSort);
        List<PiCustomizeTableField> fieldList = list(wrapperDevice);
        return fieldList;
    }

    /**
     * 合并字段和算法
     * @return
     */
    @Override
    public Object mergingFieldsAndAlgorithms() {
        //获取需要合并的字段集合 查询 状态为0的
        LambdaQueryWrapper<PiFieldMerge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PiFieldMerge::getStatus, PiCommonConstants.CLOSE);
        List<PiFieldMerge> fieldMergeList = iPiFieldMergeService.list(wrapper);
//        List<PiFieldMerge> fieldMergeList = iPiFieldMergeService.list();
        //原字段集合 List<String>
        List<String> sourceFieldNameList = fieldMergeList.stream()
                .map(PiFieldMerge::getSourceFieldName)
                .collect(Collectors.toList());
        //原表
        String sourceTableName = fieldMergeList.get(0).getSourceTableName();

        //获取字段集合
        LambdaQueryWrapper<PiCustomizeTableField> customizeTableFieldWrapper = new LambdaQueryWrapper<>();
        customizeTableFieldWrapper.eq(PiCustomizeTableField::getTableName, sourceTableName);
        customizeTableFieldWrapper.in(PiCustomizeTableField::getFieldName, sourceFieldNameList);
        List<PiCustomizeTableField> tableFieldList = baseMapper.selectList(customizeTableFieldWrapper);

        //获取字段集合
        Map<String, PiCustomizeTableField> fieldNameMap = tableFieldList.stream()
                .collect(Collectors.toMap(PiCustomizeTableField::getFieldName, field -> field));

        //获取目标表id
        LambdaQueryWrapper<PiCustomizeTable> customizeTableWrapper = new LambdaQueryWrapper<>();
        customizeTableWrapper.eq(PiCustomizeTable::getTableName, fieldMergeList.get(0).getTargetTableName()).last("LIMIT 1");;
        PiCustomizeTable piCustomizeTable = ipiCustomizeTableService.getOne(customizeTableWrapper,false);
        if(piCustomizeTable == null){
            return "目标表不存在";
        }
        for (PiFieldMerge piFieldMerge : fieldMergeList) {
            //创建字段
            PiCustomizeTableField piCustomizeTableField = fieldNameMap.get(piFieldMerge.getSourceFieldName());
            //目标表
            piCustomizeTableField.setTableName(piFieldMerge.getTargetTableName());
            //目标字段名称
            piCustomizeTableField.setName(piFieldMerge.getSourceFieldCn());
            piCustomizeTableField.setSubtable(null);
            //目标表id
            piCustomizeTableField.setTableId(piCustomizeTable.getId());
            piCustomizeTableField.setId( null);
            piCustomizeTableField.setFieldName( null);
            Result result = saveField(piCustomizeTableField);
            if(result.getCode() != 200){
                log.error("字段创建失败");
                continue;
            }
            //等待1秒
            //try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

            //查询新创建的字段
//            LambdaQueryWrapper<PiCustomizeTableField> wrapper = new LambdaQueryWrapper<>();
//            wrapper.eq(PiCustomizeTableField::getTableName, piFieldMerge.getTargetTableName());
//            wrapper.eq(PiCustomizeTableField::getFieldName, piFieldMerge.getSourceFieldCn());
//            //创建时间排序
//            wrapper.orderByDesc(PiCustomizeTableField::getCreateTime);
//            wrapper.last("LIMIT 1");
//            PiCustomizeTableField customizeTableField = getOne(wrapper);

            piFieldMerge.setTargetFieldName(piCustomizeTableField.getFieldName());
            //同步算法
            algorithmCrossTableCopy(piFieldMerge);
            //标记完成
            piFieldMerge.setStatus(1);
            //同步配置
            iPiFieldMergeService.updateById(piFieldMerge);
        }
        //同步配置
        //iPiFieldMergeService.saveOrUpdateBatch(fieldMergeList);
        return null;
    }

    /**
     * 算法复制(跨表)
     * @param piFieldMerge
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> algorithmCrossTableCopy(PiFieldMerge piFieldMerge) {

        //计算信号
        LambdaQueryWrapper<PiCalculateSignal> queryWrapper = new LambdaQueryWrapper<PiCalculateSignal>();
        queryWrapper.eq(PiCalculateSignal::getPiTable,piFieldMerge.getSourceTableName());
        queryWrapper.eq(PiCalculateSignal::getPiCode,piFieldMerge.getSourceFieldName());
        queryWrapper.orderByAsc(PiCalculateSignal::getCreateTime).orderByAsc(PiCalculateSignal::getId);
        queryWrapper.eq(PiCalculateSignal::getDelFlag,0);
        List<PiCalculateSignal> list = iPiCalculateSignalService.list(queryWrapper);

        if(null != list && list.size() > 0){
            for (PiCalculateSignal piCalculateSignal : list) {
                //历史计算信号id
                String calculateId = piCalculateSignal.getId();
                //计算信号-存储
                piCalculateSignal.setId(null);
                piCalculateSignal.setPiTable(piFieldMerge.getTargetTableName());
                piCalculateSignal.setPiCode(piFieldMerge.getTargetFieldName());
                //唯一约束特殊处理
                String uniqueJson = piCalculateSignal.getUniqueJson();
                uniqueJson = uniqueJson.replace("\"colCode\":\"field_0002\"","\"colCode\":\"field_0001\"");
                uniqueJson = uniqueJson.replace("\"colCode\":\"field_0003\"","\"colCode\":\"field_0002\"");
                uniqueJson = uniqueJson.replace("\"colCode\":\"field_0005\"","\"colCode\":\"field_0003\"");
                uniqueJson = uniqueJson.replace("\"colCode\":\"field_0006\"","\"colCode\":\"field_0004\"");

                piCalculateSignal.setUniqueJson(uniqueJson);

                //pi名称(目标)
                piCalculateSignal.setPiName(piFieldMerge.getSourceFieldCn());
                iPiCalculateSignalService.save(piCalculateSignal);

                //取数信号
                LambdaQueryWrapper<PiRetrievalSignal> retrievalSignalWrapper = Wrappers.lambdaQuery(PiRetrievalSignal.class);
                retrievalSignalWrapper.eq(PiRetrievalSignal::getCalculateId,calculateId);
                retrievalSignalWrapper.orderByAsc(PiRetrievalSignal::getSort).orderByAsc(PiRetrievalSignal::getId);
                retrievalSignalWrapper.eq(PiRetrievalSignal::getDelFlag,0);
                List<PiRetrievalSignal> retrievalSignalList = iPiRetrievalSignalService.list(retrievalSignalWrapper);
                //存储取数信号
                if(null != retrievalSignalList && retrievalSignalList.size() > 0){
                    for (PiRetrievalSignal piRetrievalSignal : retrievalSignalList) {
                        piRetrievalSignal.setId(null).setCalculateId(piCalculateSignal.getId());
                        iPiRetrievalSignalService.save(piRetrievalSignal);
                    }
                }

                //取数步骤
                LambdaQueryWrapper<PiRetrievalStep> retrievalStepWrapper = Wrappers.lambdaQuery(PiRetrievalStep.class);
                retrievalStepWrapper.eq(PiRetrievalStep::getCalculateId,calculateId);
                retrievalStepWrapper.eq(PiRetrievalStep::getDelFlag,0);
                List<PiRetrievalStep> retrievalStepList = iPiRetrievalStepService.list(retrievalStepWrapper);
                if(null != retrievalStepList && retrievalStepList.size() > 0){
                    for (PiRetrievalStep piRetrievalStep : retrievalStepList) {
                        piRetrievalStep.setId(null).setCalculateId(piCalculateSignal.getId());
                        iPiRetrievalStepService.save(piRetrievalStep);
                    }
                }


                //计算模型
                LambdaQueryWrapper<PiComputationModel> computationModelWrapper = Wrappers.lambdaQuery(PiComputationModel.class);
                computationModelWrapper.eq(PiComputationModel::getCalculateId,calculateId);
                computationModelWrapper.eq(PiComputationModel::getDelFlag,0);
                List<PiComputationModel> computationModelList = iPiComputationModelService.list(computationModelWrapper);
                if(null != computationModelList && computationModelList.size() > 0){
                    for (PiComputationModel piComputationModel : computationModelList) {
                        piComputationModel.setId(null).setCalculateId(piCalculateSignal.getId());
                        iPiComputationModelService.save(piComputationModel);
                    }
                }

                //计算模型(监控取值)
                LambdaQueryWrapper<PiComputationMonitorModel> computationMonitorModelWrapper = Wrappers.lambdaQuery(PiComputationMonitorModel.class);
                computationMonitorModelWrapper.eq(PiComputationMonitorModel::getCalculateId,calculateId);
                computationMonitorModelWrapper.eq(PiComputationMonitorModel::getDelFlag,0);
                List<PiComputationMonitorModel> computationMonitorModelList = iPiComputationMonitorModelService.list(computationMonitorModelWrapper);
                if(null != computationMonitorModelList && computationMonitorModelList.size() > 0){
                    for (PiComputationMonitorModel piComputationMonitorModel : computationMonitorModelList) {
                        piComputationMonitorModel.setId(null).setCalculateId(piCalculateSignal.getId());
                        iPiComputationMonitorModelService.save(piComputationMonitorModel);
                    }
                }
            }
        }
        return null;
    }


}
