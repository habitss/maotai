package com.keytech.modules.line.efficiency.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.line.efficiency.common.enums.AreaEnum;
import com.keytech.modules.line.efficiency.common.enums.OperationCmdEnum;
import com.keytech.modules.line.efficiency.dto.ManualFtDto;
import com.keytech.modules.line.efficiency.dto.ManualMessagesDto;
import com.keytech.modules.line.efficiency.dto.ProcessResult;
import com.keytech.modules.line.efficiency.entity.*;
import com.keytech.modules.line.efficiency.mapper.ManualFtLogMapper;
import com.keytech.modules.line.efficiency.service.IManualDeviceService;
import com.keytech.modules.line.efficiency.service.IManualFtLogService;
import com.keytech.modules.line.efficiency.service.IManualTableService;
import com.keytech.modules.pi.common.CommonConstants;
import com.keytech.modules.pi.dto.TableConditionDto;
import com.keytech.modules.pi.dto.TableFieldValueDto;
import com.keytech.modules.pi.dto.TableSaveDto;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.SpringContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description: 上位机系统手动操作记录
 * @Author: jeecg-boot
 * @Date:   2025-11-13
 * @Version: V1.0
 */
@Service
@Slf4j
public class ManualFtLogServiceImpl extends BaseServiceImpl<ManualFtLogMapper, ManualFtLog> implements IManualFtLogService {
    public static final int BATCH_SIZE = 500; // 每批处理500条
    //排查参数集合
    Set<String> unitCidSet = new HashSet<>(Arrays.asList("HMI_TopV", "Unit_CID","::","RcpPrd_Body","RcpCIP_Body","BBTIngredientBuff","Inp_Sim",
            "?","Cmd_RcpCtrl","Prpts_Ln_WYR","RcpPrd_HEAD","HMI","BH_Order"));
    @Autowired
    private IManualTableService iManualTableService;
    @Autowired
    private IManualFtLogService iManualFtLogService;
    @Autowired
    private IPiCustomizeTableService piCustomizeTableService;
    @Autowired
    private IManualDeviceService manualDeviceService;
    /**
     * 定时任务接口-上位机系统手动操作记录数据抽取(黑龙江)
     */
    @Override
    public void extractData() {
        //最后更新时间
        Date lastUpdateTime = null;
        //开始时间
        Timestamp lastLoadTime = null;
        //获取开始时间
        ManualTable manualTable = iManualTableService.query().one();
        if(manualTable == null){
            log.error("对照关系表未配置");
            return;
        }
        String tableName = manualTable.getTableName();
        //当前时间做结束时间
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        if(manualTable != null && manualTable.getLastUpdateTime() != null){
            //时间转换
            lastLoadTime = new Timestamp(manualTable.getLastUpdateTime().getTime());
        }
        if(lastLoadTime == null){
            //设置开始时间为3月前
            long oneYearAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90);
            lastLoadTime = new Timestamp(oneYearAgo);
        }
        log.info("开始上位机系统手动操作记录分页增量同步，时间范围: {} - {}", lastLoadTime, currentTime);
        //获取设备对照
        Map<String, String> deviceMap = manualDeviceService.queryDeviceMap();

        int page = 1;
        boolean hasMoreData = true;
        int totalProcessed = 0;

        while (hasMoreData) {
            long pageSize = BATCH_SIZE;
            long offset = (page - 1) * pageSize;

            // 使用参数分页查询
            List<ManualFtLog> dataList = iManualFtLogService.selectManualDataWithPaging(
                    lastLoadTime, currentTime, offset, pageSize);

            if (dataList.isEmpty()) {
                hasMoreData = false;
                break;
            }

            // 处理本批数据
            ProcessResult processResult = processBatchData(dataList, tableName, lastUpdateTime, deviceMap);
            totalProcessed += processResult.getCount();
            lastUpdateTime = processResult.getLastUpdateTime();

            log.info("已处理第 {} 页数据，数量: {}, 总计: {}", page, processResult.getCount(), totalProcessed);
            page++;
            // 如果返回的数据少于批次大小，说明没有更多数据了
            if (dataList.size() < pageSize) {
                hasMoreData = false;
            }
            //更新对照表最后更新时间
            manualTable.setLastUpdateTime(lastUpdateTime);
            iManualTableService.updateById(manualTable);
        }
        // 更新抽取记录
        if (totalProcessed > 0) {
            //更新对照表最后更新时间
            manualTable.setLastUpdateTime(lastUpdateTime);
            iManualTableService.updateById(manualTable);

            //更新表序号
            baseMapper.updateDateNumber(tableName);
            log.info("参数分页同步完成，共处理 {} 条数据", totalProcessed);
        }
    }


    @DS("manualsqlservice")
    @Override
    public List<ManualFtLog> selectManualDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, long pageSize) {
        return baseMapper.selectManualDataWithPaging(lastLoadTime, currentTime, offset, pageSize);
    }

    /**
     * 处理批量数据
     */
    private ProcessResult processBatchData(List<ManualFtLog> dataList , String tableName, Date lastUpdateTime, Map<String, String> deviceMap) {
        ProcessResult result = new ProcessResult();
        if (dataList.isEmpty()) {
            result.setCount(0);
            result.setLastUpdateTime(lastUpdateTime);
            return result;
        }
        // 需要写入的字段
        List<ManualFtDto> ftDtoList = new ArrayList<>();

        for (ManualFtLog manualFtLog : dataList) {
            String messageText = manualFtLog.getMessageText();
            if (StrUtil.isBlank(messageText)) {
                continue;
            }
            //排除manualFtLog.getMessageText() 包含Unit_CID的
            if (unitCidSet.stream().anyMatch(messageText::contains)) {
                continue;
            }
            //获取最大时间
            Date currentTime = manualFtLog.getTimeStmp();
            if (currentTime != null) {
                if (lastUpdateTime == null || currentTime.after(lastUpdateTime)) {
                    lastUpdateTime = currentTime;
                }
            }
            // 正则表达式匹配整个模式
            //String regex = "Write '([^']*)' to '\\[([^]]+)\\]([^.]+)\\.([^']+)'\\. Previous value was '([^']*)'\\.";
            String regex = "Write\\s+'([^']*)'\\s+to\\s+'\\[([^]]+)\\]([^.'\\[]+(?:\\[[^]]*\\])?)\\.([^']+)'\\.\\s+Previous\\s+value\\s+was\\s+'([^']*)'\\.";;
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(messageText);
            if (matcher.find()) {
                String writeValue = matcher.group(1);        // True 或 3
                String area = matcher.group(2);              // Area_FM 或 Area_BBT
                String device = matcher.group(3);            // FT72TV3 或 Unit_CIPB2
                String command = matcher.group(4);           // Mode.MCmd_Rel 或 OCDM_JumpSeqReq
                String previousValue = matcher.group(5);     // True 或 0
                String equipmentName = "";
                //区域名称转换
                area = AreaEnum.getNameByCode(area);
                // 处理不同格式
                if(command.contains("Bop") && command.contains("Parameter")){
                    // 处理 Bop[*****].Parameter[****]   *范围在1-31
                    command = OperationCmdEnum.BOP_PARAMETER.getName();
                }else if(command.contains("Cfg_Par") && command.contains("[") && command.contains("]")){
                    // 处理Cfg_Par[*]
                    command = OperationCmdEnum.CFG_PAR.getName();
                }else{
                    command = OperationCmdEnum.getNameByCode(command);
                }
                //排查设备匹配不上的
                if(!deviceMap.containsKey(device)){
                    log.info("未匹配到设备: {}", device);
                    continue;
                }else{
                    equipmentName = deviceMap.get(device);
                }

                if(StrUtil.isNotBlank(command)){
                    // 排除相同值
                    if(!writeValue.equals(previousValue)){
                        ftDtoList.add(new ManualFtDto(0,area,equipmentName ,device, manualFtLog.getTimeStmp(),
                                command, writeValue, previousValue,"", manualFtLog.getRecordId(), CommonConstants.NOT_DELETED));
                    }
                }else{
                    log.info("未匹配到命令: {}", messageText);
                    messageText = messageText.replace("'", "''");
                    ftDtoList.add(new ManualFtDto(-1,"","" ,"",
                            manualFtLog.getTimeStmp(),command, "", "", messageText,
                            manualFtLog.getRecordId(),CommonConstants.DELETED));
                }

            }else{
                //没匹配到的写入备注
                //单引处理
                messageText = messageText.replace("'", "''");
                ftDtoList.add(new ManualFtDto(-1,"","" ,"",
                        manualFtLog.getTimeStmp(),"", "", "", messageText,
                        manualFtLog.getRecordId(),CommonConstants.DELETED));
            }

        }

        // 批量插入
        for (ManualFtDto manualFtDto : ftDtoList) {
            TableSaveDto tableSaveDto = new TableSaveDto();
            tableSaveDto.setTable(tableName);
            //值
            TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
            tableFieldValueDto.setField("field_0001");
            tableFieldValueDto.setFieldVal(manualFtDto.getSerialNumber()+"");

            TableFieldValueDto tableFieldValueDto2 = new TableFieldValueDto();
            tableFieldValueDto2.setField("field_0002");
            tableFieldValueDto2.setFieldVal(manualFtDto.getProcess());

            TableFieldValueDto tableFieldValueDto3 = new TableFieldValueDto();
            tableFieldValueDto3.setField("field_0003");
            tableFieldValueDto3.setFieldVal(manualFtDto.getEquipmentName());

            TableFieldValueDto tableFieldValueDto4 = new TableFieldValueDto();
            tableFieldValueDto4.setField("field_0004");
            tableFieldValueDto4.setFieldVal(manualFtDto.getEquipmentCode());

            TableFieldValueDto tableFieldValueDto5 = new TableFieldValueDto();
            tableFieldValueDto5.setField("field_0005");
            tableFieldValueDto5.setFieldVal(manualFtDto.getModificationTime()==null?null:DateUtils.formatDateTime(manualFtDto.getModificationTime()));
            //tableFieldValueDto5.setFieldVal(DateUtils.formatDateTime(manualFtDto.getModificationTime()));

            TableFieldValueDto tableFieldValueDto6 = new TableFieldValueDto();
            tableFieldValueDto6.setField("field_0006");
            tableFieldValueDto6.setFieldVal(manualFtDto.getModifiedProperty());

            TableFieldValueDto tableFieldValueDto7 = new TableFieldValueDto();
            tableFieldValueDto7.setField("field_0007");
            tableFieldValueDto7.setFieldVal(manualFtDto.getPreviousValue());

            TableFieldValueDto tableFieldValueDto8 = new TableFieldValueDto();
            tableFieldValueDto8.setField("field_0008");
            tableFieldValueDto8.setFieldVal(manualFtDto.getCurrentValue());

            TableFieldValueDto tableFieldValueDto9 = new TableFieldValueDto();
            tableFieldValueDto9.setField("field_0009");
            tableFieldValueDto9.setFieldVal(manualFtDto.getRemark());

            TableFieldValueDto tableFieldValueDto10 = new TableFieldValueDto();
            tableFieldValueDto10.setField("del_flag");
            tableFieldValueDto10.setFieldVal(String.valueOf(manualFtDto.getDelFlag()));

            List<TableFieldValueDto> fieldList = new ArrayList<>();
            fieldList.add(tableFieldValueDto);
            fieldList.add(tableFieldValueDto2);
            fieldList.add(tableFieldValueDto3);
            fieldList.add(tableFieldValueDto4);
            fieldList.add(tableFieldValueDto5);
            fieldList.add(tableFieldValueDto6);
            fieldList.add(tableFieldValueDto7);
            fieldList.add(tableFieldValueDto8);
            fieldList.add(tableFieldValueDto9);
            fieldList.add(tableFieldValueDto10);
            tableSaveDto.setFieldList(fieldList);

            //约束条件
            TableConditionDto tableConditionDto = new TableConditionDto();
            tableConditionDto.setKey("field_0010");
            tableConditionDto.setVal(manualFtDto.getRecordId());
            List<TableConditionDto> tableConditionList = new ArrayList<>();
            tableConditionList.add(tableConditionDto);
            tableSaveDto.setConditionList(tableConditionList);
            piCustomizeTableService.tableSave(tableSaveDto);
        }
        result.setCount(dataList.size());
        result.setLastUpdateTime(lastUpdateTime);
        return result;
    }

    /**
     * 数据转换
     */
    private DbScaleweight convertToTarget(DbSourceScaleweight batches) {
        //对象复制
        DbScaleweight target = BeanUtil.copyProperties(batches, DbScaleweight.class);
//        target.setTenantId(tenantId);
//        // 设置ID
//        target.setId(batches.getTicketid());
        return target;
    }


    /**
     * 定时任务接口-上位机系统手动操作记录数据抽取(其他工厂)
     */
    @Override
    public void extractManualData() {
        //最后更新时间
        Date lastUpdateTime = null;
        //开始时间
        Timestamp lastLoadTime = null;
        //获取开始时间
        ManualTable manualTable = iManualTableService.query().one();
        if(manualTable == null){
            log.error("对照关系表未配置");
            return;
        }
        String tableName = manualTable.getTableName();
        //当前时间做结束时间
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        if(manualTable != null && manualTable.getLastUpdateTime() != null){
            //时间转换
            lastLoadTime = new Timestamp(manualTable.getLastUpdateTime().getTime());
        }
        if(lastLoadTime == null){
            //设置开始时间为3月前
            long oneYearAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90);
            lastLoadTime = new Timestamp(oneYearAgo);
        }
        log.info("开始上位机系统手动操作记录分页增量同步，时间范围: {} - {}", lastLoadTime, currentTime);
        int page = 1;
        boolean hasMoreData = true;
        int totalProcessed = 0;

        while (hasMoreData) {
            long pageSize = BATCH_SIZE;
            long offset = (page - 1) * pageSize;

            // 使用参数分页查询
            List<ManualMessagesDto> dataList = iManualFtLogService.selectManualPmDataWithPaging(
                    lastLoadTime, currentTime, offset, pageSize);

            if (dataList.isEmpty()) {
                hasMoreData = false;
                break;
            }

            // 处理本批数据
            ProcessResult processResult = processBatchPmData(dataList, tableName, lastUpdateTime);
            totalProcessed += processResult.getCount();
            lastUpdateTime = processResult.getLastUpdateTime();

            log.info("已处理第 {} 页数据，数量: {}, 总计: {}", page, processResult.getCount(), totalProcessed);
            page++;
            // 如果返回的数据少于批次大小，说明没有更多数据了
            if (dataList.size() < pageSize) {
                hasMoreData = false;
            }
            //更新对照表最后更新时间
            manualTable.setLastUpdateTime(lastUpdateTime);
            iManualTableService.updateById(manualTable);
        }
        // 更新抽取记录
        if (totalProcessed > 0) {
            //更新对照表最后更新时间
            manualTable.setLastUpdateTime(lastUpdateTime);
            iManualTableService.updateById(manualTable);

            //同步序号
//            manualTable.setLastUpdateTime(lastUpdateTime);
//            iManualTableService.updateById(manualTable);
            //更新表序号
            baseMapper.updateDateNumber(tableName);
            log.info("参数分页同步完成，共处理 {} 条数据", totalProcessed);
        }
    }

    /**
     * 分页查询
     * @param lastLoadTime
     * @param currentTime
     * @param offset
     * @param pageSize
     * @return
     */
    @DS("bmsqlservice")
    @Override
    public List<ManualMessagesDto> selectManualPmDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, long pageSize) {
        return baseMapper.selectManualPmDataWithPaging(lastLoadTime, currentTime, offset, pageSize);
    }


    /**
     * 处理批量数据
     */
    private ProcessResult processBatchPmData(List<ManualMessagesDto> dataList , String tableName, Date lastUpdateTime) {
        ProcessResult result = new ProcessResult();
        if (dataList.isEmpty()) {
            result.setCount(0);
            result.setLastUpdateTime(lastUpdateTime);
            return result;
        }
        // 批量插入
        for (ManualMessagesDto manualFtDto : dataList) {
            //获取最大时间
            Date currentTime = manualFtDto.getMsgTime();
            if (currentTime != null) {
                if (lastUpdateTime == null || currentTime.after(lastUpdateTime)) {
                    lastUpdateTime = currentTime;
                }
            }
            TableSaveDto tableSaveDto = new TableSaveDto();
            tableSaveDto.setTable(tableName);
            //值
            TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
            tableFieldValueDto.setField("field_0001");
            tableFieldValueDto.setFieldVal("0");

            TableFieldValueDto tableFieldValueDto2 = new TableFieldValueDto();
            tableFieldValueDto2.setField("field_0002");
            tableFieldValueDto2.setFieldVal(manualFtDto.getReccatName());

            TableFieldValueDto tableFieldValueDto3 = new TableFieldValueDto();
            tableFieldValueDto3.setField("field_0003");
            tableFieldValueDto3.setFieldVal(manualFtDto.getUnitName());

            TableFieldValueDto tableFieldValueDto4 = new TableFieldValueDto();
            tableFieldValueDto4.setField("field_0004");
            tableFieldValueDto4.setFieldVal(manualFtDto.getInstanceName());

            TableFieldValueDto tableFieldValueDto5 = new TableFieldValueDto();
            tableFieldValueDto5.setField("field_0005");
            tableFieldValueDto5.setFieldVal(manualFtDto.getMsgTime()==null?null:DateUtils.formatDateTime(manualFtDto.getMsgTime()));

            TableFieldValueDto tableFieldValueDto6 = new TableFieldValueDto();
            tableFieldValueDto6.setField("field_0006");

            tableFieldValueDto6.setFieldVal(manualFtDto.getClassName()+"-"+manualFtDto.getMsgText());

            TableFieldValueDto tableFieldValueDto7 = new TableFieldValueDto();
            tableFieldValueDto7.setField("field_0007");
            tableFieldValueDto7.setFieldVal("FALSE");

            TableFieldValueDto tableFieldValueDto8 = new TableFieldValueDto();
            tableFieldValueDto8.setField("field_0008");
            tableFieldValueDto8.setFieldVal("TRUE");

//            TableFieldValueDto tableFieldValueDto9 = new TableFieldValueDto();
//            tableFieldValueDto9.setField("field_0009");
//            tableFieldValueDto9.setFieldVal("");

            List<TableFieldValueDto> fieldList = new ArrayList<>();
            fieldList.add(tableFieldValueDto);
            fieldList.add(tableFieldValueDto2);
            fieldList.add(tableFieldValueDto3);
            fieldList.add(tableFieldValueDto4);
            fieldList.add(tableFieldValueDto5);
            fieldList.add(tableFieldValueDto6);
            fieldList.add(tableFieldValueDto7);
            fieldList.add(tableFieldValueDto8);
//            fieldList.add(tableFieldValueDto9);
            tableSaveDto.setFieldList(fieldList);

            //约束条件
            TableConditionDto tableConditionDto = new TableConditionDto();
            tableConditionDto.setKey("field_0010");
            tableConditionDto.setVal(manualFtDto.getDsId());
            List<TableConditionDto> tableConditionList = new ArrayList<>();
            tableConditionList.add(tableConditionDto);
            tableSaveDto.setConditionList(tableConditionList);
            piCustomizeTableService.tableSave(tableSaveDto);
        }
        result.setCount(dataList.size());
        result.setLastUpdateTime(lastUpdateTime);
        return result;
    }

}
