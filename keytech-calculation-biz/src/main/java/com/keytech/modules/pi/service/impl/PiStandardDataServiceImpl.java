package com.keytech.modules.pi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.common.NumberCalcUtil;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.fivewhy.service.IFiveWhyAnalyseService;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import com.keytech.modules.pi.dto.PiGreenDataDto;
import com.keytech.modules.pi.dto.RerunDto;
import com.keytech.modules.pi.dto.TableConditionQueryDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiStandardDataMapper;
import com.keytech.modules.pi.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: PI数据红黄区
 * @Author: aolei
 * @Date:   2024-12-16
 * @Version: V1.0
 */
@Slf4j
@Service
public class PiStandardDataServiceImpl extends BaseServiceImpl<PiStandardDataMapper, PiStandardData> implements IPiStandardDataService {

    private static final Logger logger = LoggerFactory.getLogger(PiComputationModelServiceImpl.class);

    /**
     * 自定义数据表服务
     */
    @Resource
    private IPiCustomizeTableService piCustomizeTableService;

    /**
     * 自定义数据表-字段配置信息服务
     */
    @Resource
    private IPiCustomizeTableFieldService piCustomizeTableFieldService;

    /**
     * PI红黄区主表服务
     */
    @Resource
    private IPiStandardManageService piStandardManageService;

    /**
     * PI红黄区pi明细表服务
     */
    @Resource
    private IPiStandardMaintenanceService piStandardMaintenanceService;


    /**
     * 一致性指数配置主表
     */
    @Resource
    private IConsistencyConfigZbService consistencyConfigZbService;

    /**
     * 一致性指数配置明细表
     */
    @Resource
    private IConsistencyConfigMxService consistencyConfigMxService;

    /**
     * 一致性指数配置PI表
     */
    @Resource
    private IConsistencyConfigPiService consistencyConfigPiService;

    /**
     * 5w分析
     */
    @Resource
    private IFiveWhyAnalyseService fiveWhyAnalyseService;

    @Resource
    private IPiAbnormalAnalysisService piAbnormalAnalysisService;

    @Resource
    private IPiAbnormalAnalysisEvaluationService evaluationService;

    @Resource
    private IMdmWeekService mdmWeekService;


    /**
     * 开始红黄区标准计算算法，定时任务调用后，每次计算5分钟内有数据更新的数据
     */
    @Override
    public void startStandardCalculate() {
//        logger.info("开始进入红黄区定时重算");
        //查询所有的PI基础数据表，查询出10分钟内有数据更新的数据
        //查询所有的pi基础数据表
        List<PiCustomizeTable> piCustomizeTables = piCustomizeTableService.list();

        //查询所有PI基础数据表的字段配置信息
        List<PiCustomizeTableField> customizeTableFields = piCustomizeTableFieldService.list();


        //存储所有需要计算红黄区的数据
        Map<String,List<Map<String,Object>>> listDataMap = new HashMap<>();

        //查询符合要求的所有数据
        for (PiCustomizeTable piCustomizeTable : piCustomizeTables) {
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 计算一个小时前的时间
            LocalDateTime tenMinutesAgo = now.minusDays(1);
            //设置时间格式
            String formatTime = tenMinutesAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            // 设置查询条件
            String query = "update_time >= '"+formatTime+"'";
            //设置查询字段，默认查询全部
            List<String> columns = new ArrayList<>();

            TableConditionQueryDto tableConditionQueryDto = new TableConditionQueryDto();
            tableConditionQueryDto.setFieldList(columns);
            tableConditionQueryDto.setTable(piCustomizeTable.getTableName());
            tableConditionQueryDto.setCondition(query);

            //当前表中所有的符合要求的数据
            List<Map<String,Object>> tableData = piCustomizeTableService.getTableByCondition(tableConditionQueryDto);

            if(null != tableData && tableData.size() > 0){
                listDataMap.put(piCustomizeTable.getTableName(),tableData);
            }
        }

        List<String> tables = listDataMap.keySet().stream().toList();
        //所有符合要求的标准
        List<PiStandardManage> piStandardManageList = new ArrayList<>();
        if(null != tables && tables.size() > 0){
            //查询表所有的标准
            LambdaQueryWrapper<PiStandardManage> queryWrapper = Wrappers.lambdaQuery(PiStandardManage.class);
            queryWrapper.in(PiStandardManage::getTableName,tables);
            queryWrapper.orderByAsc(PiStandardManage::getSort);
            piStandardManageList = piStandardManageService.list(queryWrapper);
        }

        //根据标准表，查询对应所有明细数据
        List<String> manageIds = piStandardManageList.stream()
                .map(PiStandardManage::getId)
                .collect(Collectors.toList());
        //查询明细
        List<PiStandardMaintenance> standardMaintenanceList = new ArrayList<>();
        if (null != manageIds && manageIds.size() > 0){
            LambdaQueryWrapper<PiStandardMaintenance> queryWrapper = Wrappers.lambdaQuery(PiStandardMaintenance.class);
            queryWrapper.in(PiStandardMaintenance::getManageId,manageIds);
            standardMaintenanceList = piStandardMaintenanceService.list(queryWrapper);
        }

        if(listDataMap.size() > 0 && piStandardManageList.size() > 0 && standardMaintenanceList.size() > 0){
//            logger.info("红黄区定时重算，调用standardCalculate方法，参数listDataMap："+listDataMap+"，参数piStandardManageList："+piStandardManageList+"，参数standardMaintenanceList："+standardMaintenanceList);
            standardCalculate(customizeTableFields,listDataMap,piStandardManageList,standardMaintenanceList,false);
        }
    }

    /**
     * 重算红黄区
     * @param rerunDto
     */
    @Override
    public void reSunStandardCalculate(RerunDto rerunDto){
        //查询表中标准配置的重算时间字段
        //查询表所有的标准
        LambdaQueryWrapper<PiStandardManage> queryWrapper = Wrappers.lambdaQuery(PiStandardManage.class);
        queryWrapper.eq(PiStandardManage::getTableName,rerunDto.getTableName());
        queryWrapper.orderByAsc(PiStandardManage::getSort);
        List<PiStandardManage> piStandardManageList = piStandardManageService.list(queryWrapper);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startDateStr = sdf.format(rerunDto.getTimeStart());
        String endDateStr = sdf.format(rerunDto.getTimeEnd());
        if (null != piStandardManageList && piStandardManageList.size() > 0){
            String queryField = piStandardManageList.get(0).getCalcTimeField();
            String query = queryField + " >= '" + startDateStr + "' and " + queryField + " <= '" + endDateStr + "'";

            TableConditionQueryDto tableConditionQueryDto = new TableConditionQueryDto();

//            List<String> fields = rerunDto.getFieldList();
//            if(fields.size() > 0){
//                tableConditionQueryDto.setFieldList(fields);
//            }else{
//                tableConditionQueryDto.setFieldList(new ArrayList<>());
//            }
            //默认查询所有的
            tableConditionQueryDto.setFieldList(new ArrayList<>());

            tableConditionQueryDto.setTable(rerunDto.getTableName());
            tableConditionQueryDto.setCondition(query);
            //当前表中所有的符合要求的数据
            List<Map<String,Object>> tableData = piCustomizeTableService.getTableByCondition(tableConditionQueryDto);

            //根据标准表，查询对应所有明细数据
            List<String> manageIds = piStandardManageList.stream()
                    .map(PiStandardManage::getId)
                    .collect(Collectors.toList());
            //查询明细
            List<PiStandardMaintenance> standardMaintenanceList = new ArrayList<>();
            if (null != manageIds && manageIds.size() > 0){
                LambdaQueryWrapper<PiStandardMaintenance> queryWrapperMaintenance = Wrappers.lambdaQuery(PiStandardMaintenance.class);
                queryWrapperMaintenance.in(PiStandardMaintenance::getManageId,manageIds);
                standardMaintenanceList = piStandardMaintenanceService.list(queryWrapperMaintenance);
            }

            if(tableData.size() > 0 && piStandardManageList.size() > 0 && standardMaintenanceList.size() > 0){
                Map<String,List<Map<String,Object>>> listDataMap = new HashMap<>();
                listDataMap.put(rerunDto.getTableName(),tableData);

                //查询所有PI基础数据表的字段配置信息
                List<PiCustomizeTableField> customizeTableFields = piCustomizeTableFieldService.lambdaQuery()
                        .eq(PiCustomizeTableField::getTableName,rerunDto.getTableName()).list();

                standardCalculate(customizeTableFields,listDataMap,piStandardManageList,standardMaintenanceList,true);
            }
        }
    }

    /**
     * 对所有的明细数据做对应的红黄区
     * @param customizeTableFields 字段配置信息
     * @param listDataMap 明细数据，key是表名，value是list数据
     * @param piStandardManageList 数据表所有的标准
     * @param standardMaintenanceList 数据表所有的标准明细
     * @param ifRerun 是否重算
     */
    public void standardCalculate(
            List<PiCustomizeTableField> customizeTableFields,
            Map<String,List<Map<String,Object>>> listDataMap,
            List<PiStandardManage> piStandardManageList,
            List<PiStandardMaintenance> standardMaintenanceList,
            boolean ifRerun
    ){
        //查询所有的表名
        List<String> standardTableNameList = listDataMap.keySet().stream().toList();

        //根据表名进行数据归类,标准归类
        // 使用stream和groupingBy按tableName分类
        Map<String, List<PiStandardManage>> classifiedMap = piStandardManageList.stream()
                .collect(Collectors.groupingBy(PiStandardManage::getTableName));

        //数据标准存储集合
        List<PiStandardData> listStandardData = new ArrayList<>();

        for(String tableName : standardTableNameList){
            //获取表中对应的明细数据
            List<Map<String,Object>> tableData = listDataMap.get(tableName);
            //表的标准
            List<PiStandardManage> tableStandards = classifiedMap.get(tableName);

            if(null == tableStandards || tableStandards.size() == 0){
                continue;
            }

            List<String> standardManageIds = tableStandards.stream()
                    .map(PiStandardManage::getId)
                    .collect(Collectors.toList());

            List<PiStandardMaintenance> standardMaintenanceListTp = standardMaintenanceList.stream().filter(e->
                    standardManageIds.contains(e.getManageId())
            ).collect(Collectors.toList());

            //获取表的字段配置信息
            List<PiCustomizeTableField> customizeTableFieldByTableName = customizeTableFields.stream().filter(e->e.getTableName().equals(tableName)).collect(Collectors.toList());

            if(null != tableStandards && tableStandards.size() > 0 &&  null != standardMaintenanceListTp && standardMaintenanceListTp.size() > 0){
                //根据数据来判断标准
                for (Map<String,Object> data : tableData){
                    PiStandardData piStandardData = new PiStandardData();
                    //获取标准
                    List<PiStandardMaintenance> standardMaintenanceListTpOne = getPiStandardMaintenanceByData(data,tableStandards,standardMaintenanceListTp);
                    String json = getRedYellowJson(data,standardMaintenanceListTpOne,customizeTableFieldByTableName);
                    if(StrUtil.isNotBlank(json)){
                        piStandardData.setTableName(tableName);
                        piStandardData.setDataId(data.get("id").toString());
                        piStandardData.setStandardJson(json);

                        List<String> manageIds = standardMaintenanceListTpOne.stream().map(PiStandardMaintenance::getManageId).distinct().collect(Collectors.toList());
                        piStandardData.setManageId(String.join(",",manageIds));

                        piStandardData.setUpdateTime(new Date());
                        listStandardData.add(piStandardData);
                    }
                }
            }
        }
        if(null != listStandardData && listStandardData.size() > 0){
            saveStandarData(listStandardData);
        }
    }

    /**
     * 根据表的标准集合和标准明细集合，返回单条数据的标准明细
     * @param data 一条数据
     * @param manages 此数据对应数据表的标准集合
     * @param maintenances 此数据对应数据表的明细集合
     * @return
     */
    public List<PiStandardMaintenance> getPiStandardMaintenanceByData(Map<String,Object> data,List<PiStandardManage> manages,
                                                                      List<PiStandardMaintenance> maintenances){

        //对标准进行排序，按照条件个数进行排序，把条件最多的放在最上面
        // 使用 Collections.sort 进行排序
//        Collections.sort(manages, new Comparator<PiStandardManage>() {
//            @Override
//            public int compare(PiStandardManage o1, PiStandardManage o2) {
//                if(StrUtil.isNotBlank(o1.getConditionJson()) &&  StrUtil.isNotBlank(o2.getConditionJson())){
//                    //获取条件集合的json
//                    String queryJsonO1 = o1.getConditionJson();
//                    // 使用 JSONUtil 将 JSON 字符串转换为 Map 对象
//                    List<Map> listMapO1 = JSONUtil.toList(queryJsonO1, Map.class);
//                    //获取条件集合的json
//                    String queryJsonO2 = o2.getConditionJson();
//                    // 使用 JSONUtil 将 JSON 字符串转换为 Map 对象
//                    List<Map> listMapO2 = JSONUtil.toList(queryJsonO2, Map.class);
//                    System.out.println(Integer.compare(listMapO2.size(), listMapO1.size()));
//                    return Integer.compare(listMapO2.size(), listMapO1.size());
//                }else {
//                    return 0;
//                }
//            }
//        });
        Collections.sort(manages, new Comparator<PiStandardManage>() {
            @Override
            public int compare(PiStandardManage o1, PiStandardManage o2) {
                // Get lists from JSON strings
                List<Map> listMapO1 = getListFromJson(o1.getConditionJson());
                List<Map> listMapO2 = getListFromJson(o2.getConditionJson());
                // Compare the sizes of the lists
                return Integer.compare(listMapO2.size(), listMapO1.size());
            }
        });

        //需要返回的标准明细
        List<PiStandardMaintenance> piStandardMaintenanceList = new ArrayList<>();

        Date date = new Date();

        //根据是否重算，来过滤对应的标准
        String reRunFieldTime = null != data.get(manages.get(0).getCalcTimeField()) ? data.get(manages.get(0).getCalcTimeField()).toString() : "";
        reRunFieldTime = reRunFieldTime.replace("T"," ");
        if(reRunFieldTime.length() == 16){
            reRunFieldTime = reRunFieldTime + ":00";
        }
        if(reRunFieldTime.length() == 10){
            reRunFieldTime = reRunFieldTime + " 00:00:00";
        }
        if(StrUtil.isNotBlank(reRunFieldTime)){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = sdf.parse(reRunFieldTime);
            } catch (ParseException e) {
                e.printStackTrace();
                return piStandardMaintenanceList;
            }
        }else{
            return piStandardMaintenanceList;
        }
        //按照有效期来过滤标准
        Date finalDate = date;
        manages = manages.stream().filter(e->
                !Objects.isNull(e.getEnableTime()) &&
                e.getEnableTime().before(finalDate) &&
                (Objects.isNull(e.getStopTime()) || (!Objects.isNull(e.getStopTime()) && e.getStopTime().after(finalDate)))
        ).collect(Collectors.toList());

        //标准主表信息
        List<PiStandardManage> piStandardManageList = new ArrayList<>();

        for(PiStandardManage manage : manages){
            try{
                //获取条件集合的json
                String queryJson = manage.getConditionJson();
                // 使用 JSONUtil 将 JSON 字符串转换为 Map 对象
                List<Map> listMap = JSONUtil.toList(queryJson, Map.class);
                int i = 0;
                for(Map map : listMap){
                    //字段名称
                    String field = map.get("field").toString();
                    //字段标准值
                    String value = map.get("value").toString().trim();
                    //条件
                    String symbol = map.get("symbol").toString();

                    //获取实际值
                    String actualValue = !Objects.isNull(data.get(field)) ? data.get(field).toString() : "";

                    if(actualValue.endsWith(".00") || actualValue.endsWith(".0")){
                        actualValue = actualValue.split("\\.")[0];
                    }
                    if(value.endsWith(".00") || value.endsWith(".0")){
                        value = value.split("\\.")[0];
                    }
//            1 > 2>= 3= 4 != 5< 6 <=
                    boolean bj = false;
                    switch (symbol){
                        case "1" :
                            if(Double.parseDouble(actualValue) > Double.parseDouble(value)){
                                bj = true;
                            }
                            break;
                        case "2" :
                            if(Double.parseDouble(actualValue) >= Double.parseDouble(value)){
                                bj = true;
                            }
                            break;
                        case "3" :
                            //不区分大小写
                            if(StringUtils.equalsIgnoreCase(actualValue,value)){
                                bj = true;
                            }
                            break;
                        case "4" :
                            //不区分大小写
                            if(!StringUtils.equalsIgnoreCase(actualValue,value)){
                                bj = true;
                            }
                            break;
                        case "5" :
                            if(Double.parseDouble(actualValue) < Double.parseDouble(value)){
                                bj = true;
                            }
                            break;
                        case "6" :
                            if(Double.parseDouble(actualValue) <= Double.parseDouble(value)){
                                bj = true;
                            }
                            break;
                        default:
                    }
                    if(bj){
                        i++;
                    }else{
                        break;
                    }
                }
                if(i == listMap.size()){
                    piStandardManageList.add(manage);
                    List<PiStandardMaintenance>  piStandardMaintenanceListTemp = maintenances.stream().filter(e->e.getManageId().equals(manage.getId())).collect(Collectors.toList());
                    //查询返回标准明细中的PI是否存在于当前标准明细中，如果存在，不写入，不存在写入

                    List<String> fields = piStandardMaintenanceList.stream().map(PiStandardMaintenance::getFieldName).collect(Collectors.toList());
                    piStandardMaintenanceListTemp = piStandardMaintenanceListTemp.stream().filter(e -> !fields.contains(e.getFieldName())).collect(Collectors.toList());
                    piStandardMaintenanceList.addAll(piStandardMaintenanceListTemp);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
//        if(null != piStandardManageList && piStandardManageList.size() > 0){
//            //所有符合要求的标准
//            List<String> manageIds = piStandardManageList.stream().map(PiStandardManage::getId).collect(Collectors.toList());
//            piStandardMaintenanceList = maintenances.stream().filter(e->manageIds.contains(e.getManageId())).collect(Collectors.toList());
//        }
        return piStandardMaintenanceList;
    }

    /**
     * 计算PI的红黄区，并且封装成json字符串
     * @param data 数据
     * @param piStandardMaintenanceList 标准
     * @param customizeTableFieldByTableName 字段配置信息
     * @return
     */
    @Override
    public String getRedYellowJson(Map<String,Object> data,List<PiStandardMaintenance> piStandardMaintenanceList,List<PiCustomizeTableField> customizeTableFieldByTableName){
        //格式：field：field4，color：red，manage_id，maintenance_id
        String json = "";
        List<HashMap<String,String>> listMap = new ArrayList<>();
        //获取所有的字段名称
        for(PiStandardMaintenance standardMaintenance : piStandardMaintenanceList){
            try{
                long fieldCount = 0;
                //判断此PI是否已经判断了红黄区，如果判断了，后面就无需判断
                if(null != listMap && listMap.size() > 0){
                    fieldCount = listMap.stream().filter(e-> null != e.get("field") && e.get("field").equals(standardMaintenance.getFieldName())).count();
                }

                HashMap<String,String> map = new HashMap<>();
                map.put("field",standardMaintenance.getFieldName());
                map.put("manage_id",standardMaintenance.getManageId());
                map.put("maintenance_id",standardMaintenance.getId());
                map.put("color","");

                if(fieldCount > 0){
//                    listMap.add(map);
                    continue;
                }
                String fieldName = (null == standardMaintenance.getFieldName() || standardMaintenance.getFieldName().equals("null")) ? "" : standardMaintenance.getFieldName();
                //实际值-字符串
                String practicalValueStr = (StrUtil.isBlank(fieldName) || null == data.get(fieldName)) ? "" : data.get(fieldName).toString();

                practicalValueStr = practicalValueStr.replace("/","").trim();
                //判断数据是否是数值
                String regex = "^[+-]?\\d*(\\.\\d+)?$";

                if(StrUtil.isBlank(practicalValueStr) || !practicalValueStr.matches(regex)){
                    listMap.add(map);
                    continue;
                }

                //红黄区、标准值任意一个值为空，不做判断
                if(Objects.isNull(standardMaintenance.getRedUp()) || Objects.isNull(standardMaintenance.getYellowUp())
                        || Objects.isNull(standardMaintenance.getRedDown()) || Objects.isNull(standardMaintenance.getYellowDown())
                        || Objects.isNull(standardMaintenance.getStandardValue())
                ){
                    listMap.add(map);
                    continue;
                }
                //实际值-数值
//                Double practicalValue = Double.parseDouble(practicalValueStr);
                BigDecimal bd = new BigDecimal(practicalValueStr);
                Double practicalValue = bd.doubleValue();

                if(null != customizeTableFieldByTableName && customizeTableFieldByTableName.size() > 0){
                    //确认保留小数几位
                    Optional<PiCustomizeTableField> fieldOptional = customizeTableFieldByTableName.stream().filter(e->e.getFieldName().equals(fieldName)).findFirst();
                    if(fieldOptional.isPresent()){
                        PiCustomizeTableField tableField = fieldOptional.get();
                        //String类型做保留数据计算
                        if("1".equals(tableField.getType()) || "5".equals(tableField.getType())){
                            if(null != tableField.getKeepDecimals()){
                                int keepDecimals = 2;
                                if(!Objects.isNull(tableField.getKeepDecimals())){
                                    keepDecimals = tableField.getKeepDecimals();
                                }
                                String takingValue = StrUtil.isBlank(tableField.getTakingValue()) ? "" : tableField.getTakingValue();
                                switch (takingValue){
                                    case "1" :
                                        //向上取整
                                        BigDecimal trueRoundedUp = bd.setScale(keepDecimals, RoundingMode.CEILING);
                                        practicalValue = trueRoundedUp.doubleValue();
                                        break;
                                    case "2" :
                                        //向下取整
                                        BigDecimal truncated = bd.setScale(keepDecimals, RoundingMode.DOWN).setScale(tableField.getKeepDecimals(), RoundingMode.HALF_UP);
                                        practicalValue = truncated.doubleValue();
                                        break;
                                    case "3" :
                                        //四舍五入
                                        BigDecimal roundedUp = bd.setScale(keepDecimals, RoundingMode.HALF_UP);
                                        practicalValue = roundedUp.doubleValue();
                                        break;
                                    case "4" :
                                        //直接截取
                                        BigDecimal roundedJq = bd.setScale(keepDecimals, RoundingMode.DOWN);
                                        practicalValue = roundedJq.doubleValue();
                                        break;
                                        //设置了保留几位小数，但是没有设置取数方式，系统默认4舍5入
                                    default:
                                        //四舍五入
                                        BigDecimal roundeDefault = bd.setScale(keepDecimals, RoundingMode.HALF_UP);
                                        practicalValue = roundeDefault.doubleValue();
                                        break;
                                }
                            }
                        }
                    }
                }

                //1实际值 2偏差值
                if("2".equals(standardMaintenance.getIndicatorType())){
                    practicalValue = NumberUtil.sub(practicalValue, standardMaintenance.getStandardValue());
                }
                //调整算法，先算绿区，再算黄区，最后算红区
                //大于等于黄下或者小于等于黄上显示绿区
                //大于等于红下、小于黄下或者小于等于红上大于黄上显示黄区
                if(practicalValue >= standardMaintenance.getYellowDown() && practicalValue <= standardMaintenance.getYellowUp()){
                    map.put("color","green");
                }else if
                (
                        (practicalValue >= standardMaintenance.getRedDown() && practicalValue < standardMaintenance.getYellowDown())
                                ||
                        (practicalValue <= standardMaintenance.getRedUp() && practicalValue > standardMaintenance.getYellowUp())

                )
                {
                    map.put("color","yellow");
                }else{
                    map.put("color","red");
                }
//                //判断红区
//                if(practicalValue > standardMaintenance.getRedUp() || practicalValue < standardMaintenance.getRedDown()){
//                    map.put("color","red");
//                }
//                //判断黄区
//                else if
//                (
//                        (practicalValue <= standardMaintenance.getRedUp() && practicalValue > standardMaintenance.getYellowUp()) ||
//                                (practicalValue < standardMaintenance.getYellowDown() && practicalValue >= standardMaintenance.getRedDown())
//                )
//                {
//                    map.put("color","yellow");
//                }
//                else{
//                    map.put("color","green");
//                }
                listMap.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(listMap.size() > 0){
            json = JSONUtil.toJsonStr(listMap);
        }
        return json;
    }



    /**
     * 保存pi数据的红黄区
     * @param listStandardData
     */
    public void saveStandarData(List<PiStandardData> listStandardData){
        //根据数据的data_id来判断数据表中是否存在，存在的话，将id设置到数据中，做更新处理，不存在做新增处理
        List<String> dataIds = listStandardData.stream()
                .map(PiStandardData::getDataId).distinct().collect(Collectors.toList());

        List<PiStandardData> standardDataHave =  lambdaQuery()
                        .in(PiStandardData::getDataId,dataIds)
                        .list();

        for(PiStandardData standardData : listStandardData){
            List<PiStandardData> dataList =  standardDataHave.stream().filter(e ->
                    e.getDataId().equals(standardData.getDataId()) &&
                    e.getTableName().equals(standardData.getTableName())
            ).collect(Collectors.toList());
            if(dataList.size() > 0){
                standardData.setId(dataList.get(0).getId());
                //存在的话，新增需求，灰色的不更新颜色 20241218
                //表中已经存在的json
                String jsonHave = dataList.get(0).getStandardJson();
                List<Map> listMapHave = JSONUtil.toList(jsonHave, Map.class);

                //筛选出灰色的字段
                listMapHave = listMapHave.stream().filter(e -> null != e.get("color") && !"".equals(e.get("color").toString()) && e.get("color").equals("grey")).collect(Collectors.toList());
                //灰色字段集合
                List<String> fieldNames = listMapHave.stream()
                        .map(map -> (String) map.get("field"))
                        .collect(Collectors.toList());

                //重算后最新的json
                String jsonNew = standardData.getStandardJson();
                List<Map> listMapNew = JSONUtil.toList(jsonNew, Map.class);

                if(null != fieldNames && fieldNames.size() > 0){
//                    //循环新颜色集合，将灰色的填充
//                    for(Map mapTemp : listMapNew){
//                        if(fieldNames.contains(mapTemp.get("field"))){
//                            mapTemp.put("color","grey");
//                            mapTemp.put("maintenance_id","");
//                            mapTemp.put("manage_id","");
//                        }
//                    }
                    //如果灰色字段不在新颜色集合中，全部新增到新颜色集合中
                    for(String fieldName : fieldNames){
                        boolean isInsert = true;
                        for(Map mapTemp : listMapNew){
                            if(fieldName.equals(mapTemp.get("field"))){
                                mapTemp.put("color","grey");
                                mapTemp.put("maintenance_id","");
                                mapTemp.put("manage_id","");
                                isInsert = false;
                                break;
                            }
                        }
                        if(isInsert){
                            Map mapTemp = new HashMap<>();
                            mapTemp.put("field",fieldName);
                            mapTemp.put("color","grey");
                            mapTemp.put("maintenance_id","");
                            mapTemp.put("manage_id","");
                            listMapNew.add(mapTemp);
                        }
                    }
                }
                if(listMapNew.size() > 0){
                    jsonNew = JSONUtil.toJsonStr(listMapNew);
                    standardData.setStandardJson(jsonNew);
                }else{
                    standardData.setStandardJson(null);
                }
            }else{
                standardData.setCreateTime(new Date());
            }
        }
        if(null != listStandardData && listStandardData.size() > 0){
            saveOrUpdateBatch(listStandardData);
        }
    }


    /**
     * 剔除PI数据，将pi显示成灰色
     * @param tableCode 表名称
     * @param piCodes PI集合
     * @param dataId 数据id
     */
    @Override
    public void setPiColorToGrey(String tableCode,List<String> piCodes,String dataId){
        //查询数据是否存在
        List<PiStandardData> standardDataHave =  lambdaQuery()
                .eq(PiStandardData::getTableName,tableCode)
                .eq(PiStandardData::getDataId,dataId)
                .list();
        PiStandardData piStandardData = new PiStandardData();
        if(null != standardDataHave && standardDataHave.size() == 1){
            piStandardData = standardDataHave.get(0);
        }
        piStandardData.setTableName(tableCode);
        piStandardData.setDataId(dataId);

        //灰色的PI集合
        List<Map> newColorMap = new ArrayList<>();
        for(String picode : piCodes){
            Map colorMap = new HashMap<>();
            colorMap.put("field",picode);
            colorMap.put("color","grey");
            colorMap.put("manage_id","");
            colorMap.put("maintenance_id","");
            newColorMap.add(colorMap);
        }
        //原PI颜色
        String standardJson = piStandardData.getStandardJson();
        if(StrUtil.isNotBlank(standardJson)){
            List<Map>  listMapHave = JSONUtil.toList(standardJson, Map.class);
            listMapHave = listMapHave.stream().filter(e -> !piCodes.contains(e.get("field"))).collect(Collectors.toList());
            newColorMap.addAll(listMapHave);
        }
        //将map集合转换成json
        piStandardData.setStandardJson(JSONUtil.toJsonStr(newColorMap));
        saveOrUpdate(piStandardData);
    }


    /**
     * PI绿区率查询
     * @param tableName 表中文名称
     * @param tableId 表ID
     * @param tableCode 表英文名称
     * @param queryField 查询字段名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    @Override
    public List<PiGreenDataDto> piGreenData(String tableName,String tableId,String tableCode,String queryField,String startTime, String endTime){

        List<PiGreenDataDto> retDataList = new ArrayList<>();
        //查询配置表，查询找需要统计的绿区率PI
        List<PiCustomizeTableField> customizeTableFieldList = piCustomizeTableFieldService.lambdaQuery()
                .eq(PiCustomizeTableField::getTableId,tableId)
                .eq(PiCustomizeTableField::getIsStatistics, "1")
                .orderByAsc(PiCustomizeTableField::getSort)
                .list();
        if(CollUtil.isEmpty(customizeTableFieldList)){
            return retDataList;
        }

        //获取字段清单
        List<String> fields = customizeTableFieldList.stream().map(PiCustomizeTableField::getFieldName).collect(Collectors.toList());
        //查询PI的绿区率
        retDataList = getPiGreenData(tableCode,fields,queryField,startTime,endTime, false);
        //将表中文名称和字段中文名称拼接上去
        calcDataList(retDataList, customizeTableFieldList, tableName, tableCode, null);
        return retDataList;
    }

    @Override
    public List<PiGreenDataDto> getPayCloseAttentionPi(String factoryCode, String deptCode, String isFocus, String startTime, String endTime) {
        //返回值
        List<PiGreenDataDto> retDataList = new ArrayList<>();
        //获取表清单
        List<PiCustomizeTable> tableList = piCustomizeTableService.lambdaQuery()
                .eq(StrUtil.isNotBlank(deptCode), PiCustomizeTable::getDeptCode, deptCode)
                .list();
        if(CollUtil.isEmpty(tableList)){
            return retDataList;
        }
        //提取每个表对象的 id
        List<String> tableIds = tableList.stream()
                .map(PiCustomizeTable::getId)
                .toList();
        //查询配置表，查询找重点关注且需要统计的绿区率PI
        List<PiCustomizeTableField> customizeTableFieldList = piCustomizeTableFieldService.lambdaQuery()
                .in(PiCustomizeTableField::getTableId, tableIds)
                .eq(PiCustomizeTableField::getIsStatistics, "1")
                .eq(StrUtil.isNotBlank(isFocus), PiCustomizeTableField::getIsFocus, "1")
                .orderByAsc(PiCustomizeTableField::getSort)
                .list();
        if(CollUtil.isEmpty(customizeTableFieldList)){
            return retDataList;
        }

        //将表字段根据表分组
        Map<String, List<PiCustomizeTableField>> tableFieldGroupMap = customizeTableFieldList.stream()
                .collect(Collectors.groupingBy(PiCustomizeTableField::getTableId));
        for(PiCustomizeTable piCustomizeTable: tableList){
            if(tableFieldGroupMap.containsKey(piCustomizeTable.getId())){
                List<PiCustomizeTableField> tampFieldList = tableFieldGroupMap.get(piCustomizeTable.getId());
                //获取字段清单
                List<String> fields = tampFieldList.stream()
                        .map(PiCustomizeTableField::getFieldName).toList();
                //查询PI的绿区率
                List<PiGreenDataDto> dataList = getPiGreenData(piCustomizeTable.getTableName(), fields, null, startTime, endTime, true);
                //将表中文名称和字段中文名称拼接上去
                calcDataList(dataList, tampFieldList, piCustomizeTable.getName(), piCustomizeTable.getTableName(), piCustomizeTable.getLineCode());
                retDataList.addAll(dataList);
            }
        }
        return retDataList;
    }


    /**
     * 获取一致性指数（主表明细）
     * @param zbList 指数zbid集合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    @Override
    public List<ConsistencyConfigZb> getConsistencyIndexBySum(List<ConsistencyConfigZb> zbList,String startTime, String endTime){
        if(null == zbList || zbList.size() == 0){
            return zbList;
        }
        List<String> zbId = zbList.stream().map(ConsistencyConfigZb::getId).collect(Collectors.toList());
        //查询所有明细数据
        List<ConsistencyConfigMx> listMx = consistencyConfigMxService.lambdaQuery()
                .in(ConsistencyConfigMx::getZbId,zbId)
                .orderByAsc(ConsistencyConfigMx::getSort)
                .list();

        for(ConsistencyConfigZb zbEntity : zbList){
            List<ConsistencyConfigMx> mxEntity = listMx.stream().filter(e ->
                    e.getZbId().equals(zbEntity.getId())
            ).collect(Collectors.toList());

            mxEntity = getConsistencyIndexByDetails(mxEntity,startTime,endTime);
            //明细权重值合计
            double greenProportionConsistency = 0.0;
            for(ConsistencyConfigMx entityMx : mxEntity){
                greenProportionConsistency = greenProportionConsistency + entityMx.getGreenProportionConsistency();
            }
            BigDecimal bd = new BigDecimal(greenProportionConsistency);
            BigDecimal roundedUp = bd.setScale(2, RoundingMode.HALF_UP);
            zbEntity.setGreenProportionConsistency(Double.parseDouble(roundedUp.toString()));
        }
        return zbList;
    }


    /**
     * 获取一致性指数（明细表明细）
     * @param mxList 明细表ID集合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    @Override
    public List<ConsistencyConfigMx> getConsistencyIndexByDetails(List<ConsistencyConfigMx> mxList,String startTime, String endTime){
        if(null == mxList || mxList.size() == 0){
            return mxList;
        }
        //优先处理明细表中，piCode不为空的情况，piCode不为空，意味着此PI属于二级指数
        List<String> zbIds = mxList.stream().filter(e->StrUtil.isNotBlank(e.getPiCode()))
                .map(ConsistencyConfigMx::getPiCode)
                .collect(Collectors.toList());
        if(null != zbIds && zbIds.size() > 0){
            List<ConsistencyConfigZb> listZb = consistencyConfigZbService.lambdaQuery().in(ConsistencyConfigZb::getId,zbIds).list();
            listZb = getConsistencyIndexBySum(listZb,startTime,endTime);
            for(ConsistencyConfigMx mxTemp : mxList){
                for(ConsistencyConfigZb zbTemp : listZb){
                    if(StrUtil.isNotBlank(mxTemp.getPiCode()) && mxTemp.getPiCode().equals(zbTemp.getId())){
                        mxTemp.setGreenProportion(zbTemp.getGreenProportionConsistency());
                        //pi计算权重
                        double greenProportionConsistency = zbTemp.getGreenProportionConsistency() * mxTemp.getWeight() / 100.0;
                        BigDecimal bd = new BigDecimal(greenProportionConsistency);
                        BigDecimal roundedUp = bd.setScale(2, RoundingMode.HALF_UP);
                        mxTemp.setGreenProportionConsistency(roundedUp.doubleValue());
                        break;
                    }
                }
            }
        }
        //处理明细表中，piCode为空的数据
        List<String> mxIds = mxList.stream().filter(e->StrUtil.isBlank(e.getPiCode())).map(ConsistencyConfigMx::getId).collect(Collectors.toList());
        List<ConsistencyConfigPi> listPi = new ArrayList<>();
        //二级指数需要关联PI，查询所有有关联的PI
        if(null != mxIds && mxIds.size() > 0){
            listPi = consistencyConfigPiService.lambdaQuery()
                    .in(ConsistencyConfigPi::getMxId,mxIds)
                    .list();
        }
        //查询所有pi绿区率
        //按照表名进行分组
        Map<String, List<ConsistencyConfigPi>> groupedByTableCode = listPi.stream()
                .collect(Collectors.groupingBy(ConsistencyConfigPi::getTableCode));
        //获取所有的表名称
        Set<String> allKeys = groupedByTableCode.keySet();

        //所有PI的绿区率
        List<PiGreenDataDto>  greenDataAll = new ArrayList<>();

        for(String tableCode : allKeys){
            List<ConsistencyConfigPi> consistencyConfigPiArr = groupedByTableCode.get(tableCode);
            List<String> piFields = consistencyConfigPiArr.stream().map(ConsistencyConfigPi::getFieldCode).distinct().collect(Collectors.toList());
            //获取查询字段
            String queryField = consistencyConfigPiArr.get(0).getQueryField();
            if(StrUtil.isBlank(queryField) || null == piFields || piFields.size() == 0){
                continue;
            }
            List<PiGreenDataDto>  greenData = getPiGreenData(tableCode,piFields,queryField,startTime,endTime, false);
            greenDataAll.addAll(greenData);
        }

        //匹配明细表
        for(ConsistencyConfigMx mxEntity : mxList){
            if(StrUtil.isBlank(mxEntity.getPiCode())){
                List<ConsistencyConfigPi> piEntityS = listPi.stream().filter(e->e.getMxId().equals(mxEntity.getId())).collect(Collectors.toList());
                //循环PI表数据，匹配所有pi的绿区率进行合计
                long green = 0;
                long totle = 0;
                double greenProportion = 100.0;
                for(ConsistencyConfigPi piEntity : piEntityS){
                    Optional<PiGreenDataDto> piGreenDataDtoOptional = greenDataAll.stream().filter(e->
                            StrUtil.isNotBlank(e.getFieldCode()) &&
                            e.getTableCode().equals(piEntity.getTableCode()) &&
                                    e.getFieldCode().equals(piEntity.getFieldCode())
                    ).findFirst();
                    if(piGreenDataDtoOptional.isPresent()){
                        green = green + Long.parseLong(piGreenDataDtoOptional.get().getGreen());
                        totle = totle + Long.parseLong(piGreenDataDtoOptional.get().getAll());
                    }
                }
                if(totle > 0){
                    greenProportion = green / (double) totle * 100.0;
                }
                BigDecimal bd = new BigDecimal(greenProportion);
                BigDecimal roundedUp = bd.setScale(2, RoundingMode.HALF_UP);

                mxEntity.setGreen(green);
                mxEntity.setTotle(totle);
                mxEntity.setGreenProportion(roundedUp.doubleValue());

                //pi计算权重
                double greenProportionConsistency = greenProportion * mxEntity.getWeight() / 100.0;
                bd = new BigDecimal(greenProportionConsistency);
                roundedUp = bd.setScale(2, RoundingMode.HALF_UP);
                mxEntity.setGreenProportionConsistency(roundedUp.doubleValue());
            }
        }
        return mxList;
    }

    @Override
    public Map<String, Object> getPiMonWeekSummary(String deptCode, String lineCode, String year, String months, String piName) {
        Map<String, Object> resultMap = new HashMap<>();
        //查询时间
        String startTime = year+"-01-01 00:00:00";
        String endTime = year+"-12-31 23:59:59";
        //获取周月数据
        MdmWeek mdmWeek = new MdmWeek();
        mdmWeek.setYear(year);
        List<Integer> monthList;
        if(StrUtil.isNotBlank(months)){
            //获取月份集合
            monthList = Arrays.stream(months.split(","))
                    .map(Integer::parseInt)
                    .sorted()
                    .collect(Collectors.toList());
            Collections.sort(monthList);
            mdmWeek.setMonths(monthList);
        }
        List<MdmWeek> weekList = mdmWeekService.getWeek(mdmWeek);
        startTime = DateUtil.format(weekList.get(0).getStartDate(), "yyyy-MM-dd HH:mm:ss");
        endTime = DateUtil.format(weekList.get(weekList.size()-1).getEndDate(), "yyyy-MM-dd") + " 23:59:59";
        SimpleDateFormat sdf = new SimpleDateFormat("MM.dd");
        weekList.forEach(week -> {
            week.setWeekRangeStr(sdf.format(week.getStartDate())+"-"+sdf.format(week.getEndDate()));
        });
        //按月份分组
        Map<String, List<MdmWeek>> weeksGroupedByMonth = weekList.stream()
                .collect(Collectors.groupingBy(MdmWeek::getMonth));
        resultMap.put("weekList", weeksGroupedByMonth);
        //获取各项参数
        Map<String, Object> tableAndFieldInfo = getPiTableAndFieldInfo(deptCode, lineCode, null, piName);
        if (CollUtil.isEmpty(tableAndFieldInfo)) {
            return resultMap;
        }

        //报表名称map
        @SuppressWarnings("unchecked")
        Map<String, String> piTableNameMap = (Map<String, String>) tableAndFieldInfo.get("piTableNameMap");
        //按pi表分组各字段
        @SuppressWarnings("unchecked")
        Map<String, List<PiCustomizeTableField>> fieldGroupByTable = (Map<String, List<PiCustomizeTableField>>) tableAndFieldInfo.get("fieldGroupByTable");
        //重算字段集合
        @SuppressWarnings("unchecked")
        Map<String, String> recalculateTimePiMap = (Map<String, String>) tableAndFieldInfo.get("recalculateTimePiMap");
        //报表code集合
        @SuppressWarnings("unchecked")
        List<String> piTableCodes = (List<String>) tableAndFieldInfo.get("piTableCodes");
        //结果集合
        List<Map<String, Object>> resultList = new ArrayList<>();

        //查询异常分析数据
        List<PiAbnormalAnalysis> piAbnormalAnalysisList = piAbnormalAnalysisService.lambdaQuery()
                .ge(PiAbnormalAnalysis::getAbnormalTime, startTime)
                .le(PiAbnormalAnalysis::getAbnormalTime, endTime)
                .in(PiAbnormalAnalysis::getTableCode, piTableCodes)
                .list();
        Map<String,  List<PiAbnormalAnalysis>> piAbnormalAnalysisGroupedMap = piAbnormalAnalysisList.stream()
                .collect(Collectors.groupingBy(PiAbnormalAnalysis::getTableCode));

        //循环PI报表
        for(Map.Entry<String, List<PiCustomizeTableField>> entry : fieldGroupByTable.entrySet()){
            //报表id
            String tableId = entry.getKey();
            //报表字段数据
            List<PiCustomizeTableField> tempTableFieldList = entry.getValue();
            //报表重算字段，用于查询
            String calcTimeField = recalculateTimePiMap.get(tableId);
            //没有查询字段，跳过
            if(StrUtil.isBlank(calcTimeField) || CollUtil.isEmpty(tempTableFieldList)){
                continue;
            }
            //报表code
            String tableCode = tempTableFieldList.get(0).getTableName();
            //字段目标值
            Map<String, Double> fieldTargetValueMap = new HashMap<>();
            //获取所有字段名称
            List<String> fields = new ArrayList<>();
            List<String> fieldNameList = new ArrayList<>();
            for(PiCustomizeTableField p : tempTableFieldList){
                fieldNameList.add(p.getName());
                fields.add(p.getFieldName());
                fieldTargetValueMap.put(p.getFieldName(), p.getGreenTarget());
            }

            //将重算字段也加入，用于后续判断用
            String queryTimeField = calcTimeField+" as calc_field";
            fields.add(queryTimeField);
            //获取各项需要使用的数据
            Map<String, List<?>> dataMap = getDataMap(tempTableFieldList.get(0).getTableName(), fields, calcTimeField, startTime, endTime, false);
            if(CollUtil.isEmpty(dataMap)){
                continue;
            }
            //当前表中所有的符合要求的数据
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> tableData = (List<Map<String,Object>>) dataMap.get("tableData");
            //异常分析数据
            List<PiAbnormalAnalysis> abnormalList = piAbnormalAnalysisGroupedMap.get(tableCode);

            Map<String, List<Map<String,Object>>> weekTableDataMap = new HashMap<>();
            Map<String, Integer> weekAbnormalMap = new HashMap<>();
            Map<String, Integer> weekRepeatAbnormalMap = new HashMap<>();

            //处理符合数据
            for(Map.Entry<String, List<MdmWeek>> weekEntry : weeksGroupedByMonth.entrySet()) {
                //周数据
                List<MdmWeek> tempWeekList = weekEntry.getValue();
                //循环周
                for(MdmWeek mw : tempWeekList){
                    //周的开始和结束时间
                    Date startDate = mw.getStartDate();
                    Date endDate = DateUtil.endOfDay(mw.getEndDate());
                    List<Map<String, Object>> weekData = new ArrayList<>();
                    //循环数据按周获取
                    for(Map<String,Object> obj : tableData){
                        Date calcTime = MapUtil.getDate(obj, "calc_field");
                        if(isBetween(calcTime, startDate, endDate)) {
                            weekData.add(obj);
                        }
                    }
                    weekTableDataMap.put(mw.getWeekRangeStr(), weekData);

                    if(CollUtil.isNotEmpty(abnormalList)){
                        //循环异常数据
                        for(PiAbnormalAnalysis piAbnormalAnalysis :abnormalList){
                            if(isBetween(piAbnormalAnalysis.getAbnormalTime(), startDate, endDate)) {
                                //周的异常key
                                String key = piAbnormalAnalysis.getFieldCode()+"_"+mw.getWeekNumber();
                                //异常数据
                                Integer abnormalNum = weekAbnormalMap.get(key);
                                if(null == abnormalNum){
                                    abnormalNum = 0;
                                }
                                abnormalNum++;
                                weekAbnormalMap.put(key, abnormalNum);
                                //重复异常数据
                                if(1 == piAbnormalAnalysis.getState()){
                                    Integer repeatAbnormalNum = weekRepeatAbnormalMap.get(key);
                                    if(null == repeatAbnormalNum){
                                        repeatAbnormalNum = 0;
                                    }
                                    repeatAbnormalNum++;
                                    weekRepeatAbnormalMap.put(key, repeatAbnormalNum);
                                }
                            }
                        }
                    }
                }
            }
            //红黄区数据
            @SuppressWarnings("unchecked")
            List<PiStandardData> listPiStandardData = (List<PiStandardData>) dataMap.get("listPiStandardData");
            //处理红黄区数据将 listPiStandardData 转换为以 dataId 为 key 的 Map
            Map<String, PiStandardData> piStandardDataMap = listPiStandardData.stream()
                    .collect(Collectors.toMap(
                            PiStandardData::getDataId,
                            Function.identity(),
                            (existing, replacement) -> replacement // 保留后出现的
                    ));
            int index = 0;
            for(String fieldCode : fields){
                if(fieldCode.equals(queryTimeField)){
                    continue;
                }
                //目标值
                Map<String, Object> targetValueMap = new HashMap<>();
                targetValueMap.put("tableCode", piTableNameMap.get(tableId));
                targetValueMap.put("fieldCode", fieldNameList.get(index));
                targetValueMap.put("text", "目标值(%)");
                //实际值
                Map<String, Object> actualMap = new HashMap<>();
                actualMap.put("tableCode", piTableNameMap.get(tableId));
                actualMap.put("fieldCode", fieldNameList.get(index));
                actualMap.put("text", "实际值(%)");
                //总批次
                Map<String, Object> totalMap = new HashMap<>();
                totalMap.put("tableCode", piTableNameMap.get(tableId));
                totalMap.put("fieldCode", fieldNameList.get(index));
                totalMap.put("text", "总批次");
                //合格批次
                Map<String, Object> qualifiedMap = new HashMap<>();
                qualifiedMap.put("tableCode", piTableNameMap.get(tableId));
                qualifiedMap.put("fieldCode", fieldNameList.get(index));
                qualifiedMap.put("text", "绿区");
                //重复异常数
                Map<String, Object> repetitionuAbnormalMap = new HashMap<>();
                repetitionuAbnormalMap.put("tableCode", piTableNameMap.get(tableId));
                repetitionuAbnormalMap.put("fieldCode", fieldNameList.get(index));
                repetitionuAbnormalMap.put("text", "重复异常数");
                //重复异常率
                Map<String, Object> repetitionuAbnormalRateMap = new HashMap<>();
                repetitionuAbnormalRateMap.put("tableCode", piTableNameMap.get(tableId));
                repetitionuAbnormalRateMap.put("fieldCode", fieldNameList.get(index));
                repetitionuAbnormalRateMap.put("text", "重复异常率(%)");

                //循环月份
                for(Map.Entry<String, List<MdmWeek>> weekEntry : weeksGroupedByMonth.entrySet()) {
                    //月份
                    String month = weekEntry.getKey();
                    //周数据
                    List<MdmWeek> tempWeekList = weekEntry.getValue();
                    //月累实际值
                    double monActual = 0.0;
                    //月累总数
                    long monTotalNum = 0;
                    //月累合格数
                    long monQualifiedNum = 0;
                    //月累异常数
                    int monAbnormalNum = 0;
                    //月累异常重复数
                    int monAbnormalRepetitionNum = 0;
                    //循环周
                    for(MdmWeek mw : tempWeekList){
                        //红区数
                        long redNum = 0;
                        //黄区数
                        long yellowNum = 0;
                        //绿区数
                        long greenNum = 0;

                        List<Map<String,Object>> weekDataList = weekTableDataMap.get(mw.getWeekRangeStr());
                        for(Map<String,Object> obj : weekDataList){
                            String objId = MapUtil.getStr(obj, "id");
                            if(piStandardDataMap.containsKey(objId)){
                                PiStandardData standardData = piStandardDataMap.get(objId);
                                String standardJson = standardData.getStandardJson();
                                if(StrUtil.isNotBlank(standardJson)){
                                    List<Map> listMapHave = JSONUtil.toList(standardJson, Map.class);
                                    Optional<Map> fieldMapOptional = listMapHave.stream()
                                            .filter(e ->
                                                    null != e.get("field")
                                                    && !"".equals(e.get("field"))
                                                    && e.get("field").equals(fieldCode)
                                            )
                                            .findFirst();
                                    if (fieldMapOptional.isPresent()){
                                        Map fieldMap = fieldMapOptional.get();
                                        if(null != fieldMap.get("color") && !"".equals(fieldMap.get("color"))){
                                            switch (fieldMap.get("color").toString()){
                                                case "red" :
                                                    redNum ++;
                                                    break;
                                                case "yellow" :
                                                    yellowNum ++;
                                                    break;
                                                case "green" :
                                                    greenNum ++;
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //周总数
                        long totalNum = redNum + yellowNum + greenNum;
                        actualMap.put(mw.getWeekRangeStr(), getRate(totalNum, greenNum));
                        totalMap.put(mw.getWeekRangeStr(), totalNum);
                        qualifiedMap.put(mw.getWeekRangeStr(), greenNum);
                        monTotalNum += totalNum;
                        monQualifiedNum += greenNum;

                        //获取异常分析数据的key
                        String abnormalKey = fieldCode + "_" + mw.getWeekNumber();
                        int abnormalNum = null == weekAbnormalMap.get(abnormalKey) ? 0 : weekAbnormalMap.get(abnormalKey);
                        int abnormalRepetitionNum = null == weekRepeatAbnormalMap.get(abnormalKey) ? 0 : weekRepeatAbnormalMap.get(abnormalKey);
                        repetitionuAbnormalRateMap.put(mw.getWeekRangeStr(), getRate(abnormalNum, abnormalRepetitionNum));
                        repetitionuAbnormalMap.put(mw.getWeekRangeStr(), abnormalRepetitionNum);
                        monAbnormalNum += abnormalNum;
                        monAbnormalRepetitionNum += abnormalRepetitionNum;

                        //目标值
                        targetValueMap.put(mw.getWeekRangeStr(), fieldTargetValueMap.get(fieldCode));
                    }
                    actualMap.put(month, getRate(monTotalNum, monQualifiedNum));
                    totalMap.put(month, monTotalNum);
                    qualifiedMap.put(month, monQualifiedNum);
                    repetitionuAbnormalRateMap.put(month, getRate(monAbnormalNum, monAbnormalRepetitionNum));
                    repetitionuAbnormalMap.put(month, monAbnormalRepetitionNum);
                    targetValueMap.put(month, fieldTargetValueMap.get(fieldCode));
                }
                resultList.add(targetValueMap);
                resultList.add(qualifiedMap);
                resultList.add(totalMap);
                resultList.add(actualMap);
                resultList.add(repetitionuAbnormalMap);
                resultList.add(repetitionuAbnormalRateMap);
                index++;
            }
        }
        resultMap.put("resultList", resultList);
        return resultMap;
    }

    @Override
    public List<PiGreenDataDto> piGreenDataByDept(String deptCode, String lineCode, String piName,
                                                  String tableIds, String queryField, String startTime, String endTime) {
        List<PiGreenDataDto> resultDataList = new ArrayList<>();
        //获取各个数据
        Map<String, Object> tableAndFieldInfo = getPiTableAndFieldInfo(deptCode, lineCode, tableIds, piName);
        if (CollUtil.isEmpty(tableAndFieldInfo)) {
            return resultDataList;
        }
        JSONObject queryFieldJson = new JSONObject();
        if(StrUtil.isNotBlank(queryField)){
            queryFieldJson = JSONUtil.parseObj(queryField);
        }
        //按pi表分组各字段
        @SuppressWarnings("unchecked")
        Map<String, List<PiCustomizeTableField>> fieldGroupByTable = (Map<String, List<PiCustomizeTableField>>) tableAndFieldInfo.get("fieldGroupByTable");
        //报表名称map
        @SuppressWarnings("unchecked")
        Map<String, String> piTableNameMap = (Map<String, String>) tableAndFieldInfo.get("piTableNameMap");
        //重算字段集合
        @SuppressWarnings("unchecked")
        Map<String, String> recalculateTimePiMap = (Map<String, String>) tableAndFieldInfo.get("recalculateTimePiMap");
        //找需要统计的绿区率PI
        @SuppressWarnings("unchecked")
        List<PiCustomizeTableField> customizeTableFieldList = (List<PiCustomizeTableField>) tableAndFieldInfo.get("customizeTableFieldList");

        //循环PI报表
        for(Map.Entry<String, List<PiCustomizeTableField>> entry : fieldGroupByTable.entrySet()){
            //报表id
            String tableId = entry.getKey();
            //报表字段数据
            List<PiCustomizeTableField> tempTableFieldList = entry.getValue();
            //报表重算字段，用于查询
            String calcTimeField = queryFieldJson.getStr(tableId);
            //没有查询字段，跳过
            if(StrUtil.isBlank(calcTimeField) || CollUtil.isEmpty(tempTableFieldList)){
                continue;
            }
            //获取所有字段名称
            List<String> fields = tempTableFieldList.stream()
                    .map(PiCustomizeTableField::getFieldName)
                    .collect(Collectors.toList());
            //查询PI的绿区率
            List<PiGreenDataDto> tempList = getPiGreenData(tempTableFieldList.get(0).getTableName(),fields,calcTimeField,startTime,endTime, false);
            if(!tempList.isEmpty()){
                //将表中文名称和字段中文名称拼接上去
                calcDataList(tempList, customizeTableFieldList, piTableNameMap.get(tableId), tempList.get(0).getTableCode(), null);
            }
            resultDataList.addAll(tempList);
        }
        return resultDataList;
    }


    /**
     * 根据PI集合，查询对应PI的绿区率
     * @param tableCode 基础数据表-表编码
     * @param fields 查询字段集合
     * @param queryField 查询时间字段
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param isIndex 是否首页统计
     * @return 结果集合
     */
    public List<PiGreenDataDto> getPiGreenData(String tableCode, List<String> fields , String queryField, String startTime, String endTime, boolean isIndex){
        List<PiGreenDataDto> retDataList = new ArrayList<>();
        //当前表中所有的符合要求的数据
        Map<String, List<?>> dataMap = getDataMap(tableCode, fields, queryField, startTime, endTime, isIndex);
        if(CollUtil.isEmpty(dataMap)){
            return retDataList;
        }

        //查询当前数据所有的红黄区信息
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> tableData = (List<Map<String,Object>>) dataMap.get("tableData");
        @SuppressWarnings("unchecked")
        List<String> dataIds = (List<String>) dataMap.get("dataIds");
        @SuppressWarnings("unchecked")
        List<PiStandardData> listPiStandardData = (List<PiStandardData>) dataMap.get("listPiStandardData");

        List<PiAbnormalAnalysisEvaluation> piAbnormalAnalysisEvaluationList = new ArrayList<>();
        Map<String, List<FiveWhyAnalyse>> fiveWhyGroupedMap = new HashMap<>();
        Map<String, List<PiAbnormalAnalysis>> piAbnormalAnalysisGroupedMap = new HashMap<>();
        if(!isIndex){
            //查询5w数据
            List<FiveWhyAnalyse> fiveWhyAnalyseList = fiveWhyAnalyseService.lambdaQuery()
                   .in(FiveWhyAnalyse::getFieldCode, fields)
                   .in(FiveWhyAnalyse::getDataId, dataIds)
                   .eq(FiveWhyAnalyse::getTableCode, tableCode)
                   .list();
            fiveWhyGroupedMap = fiveWhyAnalyseList.stream()
                   .collect(Collectors.groupingBy(FiveWhyAnalyse::getFieldCode,
                           Collectors.collectingAndThen(Collectors.toList(),
                                   list -> list.stream().collect(Collectors.toMap(
                                           FiveWhyAnalyse::getDataId, Function.identity(), (a, b) -> a))
                                           .values().stream().toList())));

            //查询异常分析数据
            List<PiAbnormalAnalysis> piAbnormalAnalysisList = piAbnormalAnalysisService.lambdaQuery()
                   .in(PiAbnormalAnalysis::getFieldCode, fields)
                   .in(PiAbnormalAnalysis::getDataId, dataIds)
                   .eq(PiAbnormalAnalysis::getTableCode, tableCode)
                   .list();
            piAbnormalAnalysisGroupedMap = piAbnormalAnalysisList.stream()
                   .collect(Collectors.groupingBy(PiAbnormalAnalysis::getFieldCode,
                           Collectors.collectingAndThen(Collectors.toList(),
                                   list -> list.stream().collect(Collectors.toMap(
                                           PiAbnormalAnalysis::getDataId, Function.identity(), (a, b) -> a))
                                           .values().stream().toList())));

            //查询合格评价数据
            List<String> abnormalIdList = piAbnormalAnalysisList.stream()
                   .map(PiAbnormalAnalysis::getId)
                   .toList();
            if(CollUtil.isNotEmpty(abnormalIdList)){
               piAbnormalAnalysisEvaluationList = evaluationService.lambdaQuery()
                       .in(PiAbnormalAnalysisEvaluation::getRecordId, abnormalIdList)
                       .eq(PiAbnormalAnalysisEvaluation::getEvaluationResult, 1)
                       .list();
            }
        }

        //剔除的、没计算出结果的不纳入统计，其他的均纳入统计
        for(String fieldCode : fields){
            //红区数
            long redNum = 0;
            //黄区数
            long yellowNum = 0;
            //绿区数
            long greenNum = 0;
            //灰区数（剔除的）
            long greyNum = 0;

            //计算PI的红、黄、绿、灰的数量
            for(PiStandardData standardData : listPiStandardData){
                String standardJson = standardData.getStandardJson();
                if(StrUtil.isNotBlank(standardJson)){
                    List<Map> listMapHave = JSONUtil.toList(standardJson, Map.class);
                    Optional<Map> fieldMapOptional = listMapHave.stream()
                            .filter(e -> null != e.get("field") && !"".equals(e.get("field")) && e.get("field").equals(fieldCode))
                            .findFirst();
                    if (fieldMapOptional.isPresent()){
                        Map fieldMap = fieldMapOptional.get();
                        if(null != fieldMap.get("color") && !"".equals(fieldMap.get("color"))){
                            switch (fieldMap.get("color").toString()){
                                case "red" :
                                    redNum ++;
                                    break;
                                case "yellow" :
                                    yellowNum ++;
                                    break;
                                case "green" :
                                    greenNum ++;
                                    break;
                                case "grey" :
                                    greyNum ++;
                                    break;
                            }
                        }
                    }
                }
            }
            long totleNum = redNum + yellowNum + greenNum;
            //单个PI的所有数据
            PiGreenDataDto fieldVo = new PiGreenDataDto();
            fieldVo.setTableCode(tableCode);
            //字段filed编号
            fieldVo.setFieldCode(fieldCode);
            //字段红区数量
            fieldVo.setRed(String.valueOf(redNum));
            //字段黄区数量
            fieldVo.setYellow(String.valueOf(yellowNum));
            //字段绿区数量
            fieldVo.setGreen(String.valueOf(greenNum));
            //字段灰区数量（剔除）
            fieldVo.setGrey(String.valueOf(greyNum));
            //字段所有有效数
            fieldVo.setAll(String.valueOf(totleNum));

            //绿区率，默认0
            double greenProportion = 0.0;
            double yellowProportion = 0.0;
            double redProportion = 0.0;

            if(totleNum > 0){
                greenProportion = greenNum / (double)totleNum * 100.0;
                yellowProportion = yellowNum / (double)totleNum * 100.0;
                redProportion = redNum / (double)totleNum * 100.0;
            }
            BigDecimal bd = new BigDecimal(greenProportion);
            BigDecimal roundedUp = bd.setScale(2, RoundingMode.HALF_UP);
            fieldVo.setGreenProportion(roundedUp.toString());

            bd = new BigDecimal(yellowProportion);
            roundedUp = bd.setScale(2, RoundingMode.HALF_UP);
            fieldVo.setYellowProportion(roundedUp.toString());

            bd = new BigDecimal(redProportion);
            roundedUp = bd.setScale(2, RoundingMode.HALF_UP);
            fieldVo.setRedProportion(roundedUp.toString());
            if(!isIndex){
                //计算偏差分位值
                calcPercentile(fieldVo, fieldCode, tableData);
                //获取5why数据
                int fiveWhyTotal = 0;
                if(fiveWhyGroupedMap.containsKey(fieldCode) && !fiveWhyGroupedMap.get(fieldCode).isEmpty()){
                    fiveWhyTotal = fiveWhyGroupedMap.get(fieldCode).size();
                }
                fieldVo.setFiveWhyTotal(fiveWhyTotal);

                //获取异常分析数据
                int piAbnormalTotal = 0;
                int anomalyRepeatTotal = 0;
                int teamEvaluationTotal = 0;
                if(piAbnormalAnalysisGroupedMap.containsKey(fieldCode) && !piAbnormalAnalysisGroupedMap.get(fieldCode).isEmpty()){
                    List<PiAbnormalAnalysis> tempAnomalyList = piAbnormalAnalysisGroupedMap.get(fieldCode);
                    //异常数
                    piAbnormalTotal = tempAnomalyList.size();
                    //异常分类数量
                    fieldVo.setAnomalyTypeMap(tempAnomalyList.stream()
                            .collect(Collectors.groupingBy(
                                    analysis -> "dict_" + analysis.getAbnormalType(),
                                    Collectors.collectingAndThen(
                                            Collectors.counting(),
                                            Long::intValue
                                    )
                            )));
                    //重复数
                    anomalyRepeatTotal = Math.toIntExact(tempAnomalyList.stream()
                            .filter(item -> item.getState() == 1)
                            .count());

                    Set<String> tempIds = tempAnomalyList.stream()
                            .map(PiAbnormalAnalysis::getId)
                            .collect(Collectors.toSet());
                    teamEvaluationTotal = Math.toIntExact(piAbnormalAnalysisEvaluationList.stream()
                            .map(PiAbnormalAnalysisEvaluation::getRecordId)  // 假设 B 类有 getRecordId() 方法
                            .filter(tempIds::contains).count());
                }
                fieldVo.setAnomalyTotal(piAbnormalTotal);
                fieldVo.setAnomalyRepeatTotal(anomalyRepeatTotal);
                fieldVo.setAnomalyRepeatRate(NumberCalcUtil.calculatePercentage(anomalyRepeatTotal, piAbnormalTotal, false));
                fieldVo.setTeamEvaluationTotal(teamEvaluationTotal);
            }

            retDataList.add(fieldVo);
        }
        return retDataList;
    }

    private Map<String, List<?>> getDataMap(String tableCode, List<String> fields , String queryField, String startTime, String endTime, boolean isIndex){
        Map<String, List<?>> retMap = new HashMap<>();
        List<PiGreenDataDto> retDataList = new ArrayList<>();
        //查询这些字段时间范围内的所有数据
        // 设置查询条件
        String query = queryField + " <= '"+endTime+" ' and " + queryField + " >= '" + startTime + "' ";
        if(isIndex){
            query = " AND jt.field in ("+CollUtil.join(fields, ",", "'", "'")+") " +
                    " AND jt.time >= '"+startTime+"'" +
                    " AND jt.time <= '"+endTime+"'";
        }

        //设置查询字段，默认查询全部
        TableConditionQueryDto tableConditionQueryDto = new TableConditionQueryDto();
        tableConditionQueryDto.setFieldList(fields);
        tableConditionQueryDto.setTable(tableCode);
        tableConditionQueryDto.setCondition(query);
        tableConditionQueryDto.setIsIndex(isIndex);

        //当前表中所有的符合要求的数据
        List<Map<String,Object>> tableData = piCustomizeTableService.getTableByCondition(tableConditionQueryDto);
        if(CollUtil.isEmpty(tableData)){
            return null;
        }
        retMap.put("tableData", tableData);

        //查询当前数据所有的红黄区信息
        List<String> dataIds = tableData.stream().map(map -> map.get("id").toString()).collect(Collectors.toList());
        retMap.put("dataIds", dataIds);
        List<PiStandardData> listPiStandardData = this.lambdaQuery().eq(PiStandardData::getTableName,tableCode)
                .in(PiStandardData::getDataId,dataIds).list();
        retMap.put("listPiStandardData", listPiStandardData);

        //当前表中所有的符合要求的数据
        return retMap;
    }

    /**
     * 对json做处理，返回json大小
     * @param json
     * @return
     */
    List<Map> getListFromJson(String json) {
        if (StrUtil.isNotBlank(json)) {
            try {
                return JSONUtil.toList(json, Map.class);
            } catch (Exception e) {
                // Handle JSON parsing exceptions, e.g., log the error and return an empty list
                System.err.println("Error parsing JSON: " + e.getMessage());
                return List.of();
            }
        }
        // Return empty list for blank or null JSON
        return List.of();
    }

    /**
     * 计算百分位值
     * @param fieldVo 返回对象
     * @param fieldCode 字段编号
     * @param tableData 表数据
     */
    private void calcPercentile(PiGreenDataDto fieldVo, String fieldCode, List<Map<String,Object>> tableData) {
        List<Double> percentileList = new ArrayList<>();
        for(Map<String,Object> map : tableData){
            double result = 0;
            if(map.containsKey(fieldCode) && null != map.get(fieldCode)){
                Object o = map.get(fieldCode);
                try{
                    result = Double.parseDouble(o.toString());
                } catch (Exception e) {
                    result = 0;
                }
            }
            percentileList.add(result);
        }
        // 排序数据
        List<Double> sortedData = new ArrayList<>(percentileList);
        Collections.sort(sortedData);
        //保留两位小数
        DecimalFormat df = new DecimalFormat("0.00");
        //使用依赖库
        DescriptiveStatistics stats = new DescriptiveStatistics();
        sortedData.forEach(stats::addValue);
        fieldVo.setFivePercentile(df.format(stats.getPercentile(5)));
        fieldVo.setTenPercentile(df.format(stats.getPercentile(10)));
        fieldVo.setFiftyPercentile(df.format(stats.getPercentile(50)));
        fieldVo.setNinetyPercentile(df.format(stats.getPercentile(90)));
        fieldVo.setNinetyFivePercentile(df.format(stats.getPercentile(95)));
    }

    private void calcDataList(List<PiGreenDataDto> dataList, List<PiCustomizeTableField> customizeTableFieldList, String tableName, String tableCode, String lineCode){
        //将表中文名称和字段中文名称拼接上去
        if(CollUtil.isNotEmpty(dataList)){
            Map<String, String> fieldNameMap = new HashMap<>();
            Map<String, Double> greenTargetMap = new HashMap<>();
            for(PiCustomizeTableField customizeTableField : customizeTableFieldList){
                if(!customizeTableField.getTableName().equals(tableCode)){
                    continue;
                }
                fieldNameMap.put(customizeTableField.getFieldName(), customizeTableField.getName());
                greenTargetMap.put(customizeTableField.getFieldName(), customizeTableField.getGreenTarget());
            }

            for(PiGreenDataDto greenDataDto : dataList){
                greenDataDto.setTableName(tableName);
                greenDataDto.setLineCode(lineCode);
                greenDataDto.setFieldName(fieldNameMap.get(greenDataDto.getFieldCode()));
                Double greenTarget = greenTargetMap.get(greenDataDto.getFieldCode());
                greenDataDto.setGreenRateTarget(greenTarget == null ? null : greenTarget.toString());
            }
        }
    }
    //判断时间是否在某个时间段内
    public static boolean isBetween(Date target, Date start, Date end) {
        if (target == null || start == null || end == null) {
            return false;
        }
        return !target.before(start) && !target.after(end);
    }
    //计算百分比
    private static BigDecimal getRate(double total, double value) {
        double result = 0.0;
        if(total > 0){
            result = value / total * 100.0;
        }
        BigDecimal bd = new BigDecimal(result);
        return bd.setScale(2, RoundingMode.HALF_UP);
    }
    /**
     * 根据部门和产线获取PI表及相关字段信息
     * @param deptCode 部门编码
     * @param lineCode 产线编码，多个用逗号分隔
     * @param tableIds PI报表id（可选）
     * @param piName PI名称（可选）
     * @return 包含PI表信息和字段信息的Map
     */
    private Map<String, Object> getPiTableAndFieldInfo(String deptCode, String lineCode, String tableIds, String piName) {
        Map<String, Object> result = new HashMap<>();
        // 检查必要参数
        if(StrUtil.isBlank(deptCode) && StrUtil.isBlank(lineCode)){
            return null;
        }
        List<String> lineCodeList = null;
        if(StrUtil.isNotBlank(lineCode)){
            lineCodeList = Arrays.asList(lineCode.split(","));
        }
        //报表code集合
        List<String> piTableCodes = new ArrayList<>();
        //报表id集合
        List<String> piTableIds = new ArrayList<>();
        if(StrUtil.isNotBlank(tableIds)){
            piTableIds = Arrays.asList(tableIds.split(","));
        }

        //根据部门、产线获取PI表
        List<PiCustomizeTable> piCustomizeTableList = piCustomizeTableService.lambdaQuery()
                .eq(StrUtil.isNotBlank(deptCode), PiCustomizeTable::getDeptCode, deptCode)
                .in(StrUtil.isNotBlank(lineCode), PiCustomizeTable::getLineCode,  lineCodeList)
                .in(CollUtil.isNotEmpty(piTableIds), PiCustomizeTable::getId, piTableIds)
                .list();
        if(CollUtil.isEmpty(piCustomizeTableList)){
            return null;
        }
        //报表名称map
        Map<String, String> piTableNameMap = new HashMap<>();
        //重算字段集合
        Map<String, String> recalculateTimePiMap = new HashMap<>();
        for(PiCustomizeTable piCustomizeTable : piCustomizeTableList){
            if(StrUtil.isBlank(tableIds)){
                piTableIds.add(piCustomizeTable.getId());
            }
            piTableCodes.add(piCustomizeTable.getTableName());
            recalculateTimePiMap.put(piCustomizeTable.getId(), piCustomizeTable.getRecalculateTimePi());
            piTableNameMap.put(piCustomizeTable.getId(), piCustomizeTable.getName());
        }

        //查询配置表，查询找需要统计的绿区率PI
        List<PiCustomizeTableField> customizeTableFieldList = piCustomizeTableFieldService.lambdaQuery()
                .in(PiCustomizeTableField::getTableId, piTableIds)
                .eq(PiCustomizeTableField::getIsStatistics, "1")
                .like(StrUtil.isNotBlank(piName), PiCustomizeTableField::getName, piName)
                .orderByAsc(PiCustomizeTableField::getSort)
                .list();
        if(CollUtil.isEmpty(customizeTableFieldList)){
            return null;
        }
        //按pi表分组各字段
        Map<String, List<PiCustomizeTableField>> fieldGroupByTable = customizeTableFieldList.stream()
                .collect(Collectors.groupingBy(PiCustomizeTableField::getTableId));

        result.put("piCustomizeTableList", piCustomizeTableList);
        result.put("piTableNameMap", piTableNameMap);
        result.put("recalculateTimePiMap", recalculateTimePiMap);
        result.put("customizeTableFieldList", customizeTableFieldList);
        result.put("fieldGroupByTable", fieldGroupByTable);
        result.put("piTableCodes", piTableCodes);
        return result;
    }
    public static void main(String[] args) {
        Double a = 0.52;
        Double b = 0.49;
        System.out.println(NumberUtil.sub(a, b));
//        //判断数据是否是数值
//        String regex = "^[+-]?\\d*(\\.\\d+)?$";
//        String practicalValueStr = "0.37";
//
//        System.out.println(StrUtil.isBlank(practicalValueStr));
//
//        System.out.println(!practicalValueStr.matches(regex));
//
//        if(StrUtil.isBlank(practicalValueStr) || !practicalValueStr.matches(regex)){
//            System.out.println(practicalValueStr.matches(regex));
//        }

    }


}
