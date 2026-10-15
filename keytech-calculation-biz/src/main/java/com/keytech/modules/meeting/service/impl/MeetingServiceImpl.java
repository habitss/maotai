package com.keytech.modules.meeting.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.exception.entity.ExceptionReport;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import com.keytech.modules.meeting.dto.*;
import com.keytech.modules.meeting.entity.*;
import com.keytech.modules.meeting.mapper.MeetingMapper;
import com.keytech.modules.meeting.service.*;
import com.keytech.modules.meeting.utils.ObjectMerger;
import dm.jdbc.d.U;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 会议信息
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Service
public class MeetingServiceImpl extends BaseServiceImpl<MeetingMapper, Meeting> implements IMeetingService {

    @Autowired
    private IMeetingInfoService meetingInfoService;

    @Autowired
    private IMeetingTemplateService meetingTemplateService;

    @Autowired
    private IMeetingRecordService meetingRecordService;

    @Autowired
    private IMeetingRecordTemplateService meetingRecordTemplateService;

    @Autowired
    private IMeetingConfigService meetingConfigService;

    @Autowired
    private IMeetingCheckService meetingCheckService;

    @Autowired
    private ISysDepartService sysDepartService;

    @Autowired
    private IMdmWeekService mdmWeekService;

    //周映射
    static final Map<String, Integer> weekMap;
    static {
        weekMap = Map.ofEntries(
                Map.entry("MONDAY", 0),
                Map.entry("TUESDAY", 1),
                Map.entry("WEDNESDAY", 2),
                Map.entry("THURSDAY", 3),
                Map.entry("FRIDAY", 4),
                Map.entry("SATURDAY", 5),
                Map.entry("SUNDAY", 6)
        );
    }

    @Override
    public IPage<Meeting> pageList(Page<Meeting> page, LambdaQueryWrapper<Meeting> queryWrapper, String year, String month) {
        IPage<Meeting> pageList = this.page(page, queryWrapper);
        List<Meeting> records = pageList.getRecords();
        List<Meeting> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(meeting -> {
                List<MeetingRecord> meetingRecordList = meetingRecordService.list(new LambdaQueryWrapper<MeetingRecord>().eq(MeetingRecord::getMeetingId, meeting.getId()));
                if (CollectionUtils.isNotEmpty(meetingRecordList)) {
                    double score = 0;
                    for (MeetingRecord meetingRecord : meetingRecordList) {
                        if (null != meetingRecord.getScore()) {
                            score += meetingRecord.getScore();
                        }
                    }
                    meeting.setScore(score);
                }

                if (StringUtils.isNotEmpty(year) && StringUtils.isNotEmpty(month)) {
                    if (meeting.getMeetingDate() != null) {
                        Instant instant = meeting.getMeetingDate().toInstant();
                        ZoneId zoneId = ZoneId.systemDefault();
                        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
                        LocalDate localDate = zonedDateTime.toLocalDate();
                        int meetingYear = localDate.getYear();
                        int meetingMonth = localDate.getMonthValue();
                        meeting.setDay(localDate.getDayOfMonth());
                        if (Integer.parseInt(year) == meetingYear && Integer.parseInt(month) == meetingMonth) {
                            result.add(meeting);
                        }
                    }
                } else {
                    result.add(meeting);
                }
            });
        }
        return pageList.setRecords(result);
    }

    @Override
    public List<Meeting> queryList(LambdaQueryWrapper<Meeting> queryWrapper, String year, String month) {
        List<Meeting> list = this.list(queryWrapper);
        List<Meeting> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(meeting -> {
                List<MeetingRecord> meetingRecordList = meetingRecordService.list(new LambdaQueryWrapper<MeetingRecord>().eq(MeetingRecord::getMeetingId, meeting.getId()));
                if (CollectionUtils.isNotEmpty(meetingRecordList)) {
                    double score = 0;
                    for (MeetingRecord meetingRecord : meetingRecordList) {
                        if (null != meetingRecord.getScore()) {
                            score += meetingRecord.getScore();
                        }
                    }
                    meeting.setScore(score);
                }
                if (StringUtils.isNotEmpty(year) && StringUtils.isNotEmpty(month)) {
                    if (meeting.getMeetingDate() != null) {
                        Instant instant = meeting.getMeetingDate().toInstant();
                        ZoneId zoneId = ZoneId.systemDefault();
                        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
                        LocalDate localDate = zonedDateTime.toLocalDate();
                        int meetingYear = localDate.getYear();
                        int meetingMonth = localDate.getMonthValue();
                        meeting.setDay(localDate.getDayOfMonth());
                        meeting.setMonth(meetingMonth);
                        if (Integer.parseInt(year) == meetingYear && Integer.parseInt(month) == meetingMonth) {
                            result.add(meeting);
                        }
                    }
                } else {
                    result.add(meeting);
                }
            });
        }
        return result;
    }

    @Override
    public void setMeetingRecursively(SysDepart sysDepart, int orgType, Meeting meeting) {
        meeting.setDeptCode(null);
        if (orgType <= 3) {
            meeting.setFactoryCode(sysDepart.getOrgCode());
        } else {
//            SysDepart parent = sysDepartService.getById(sysDepart.getParentId());
//            setMeetingRecursively(parent, orgType - 1, meeting);
            if (orgType == 4) {
                meeting.setDeptCode(sysDepart.getOrgCode());
            } else if (orgType == 5) {
                meeting.setWorkshop(sysDepart.getOrgCode());
            } else if (orgType == 6) {
                meeting.setLineCode(sysDepart.getOrgCode());
            }
        }
    }

    @Override
    @Transactional
    public void scheduleMeeting(MeetingScheduleVO meeting, String scheduleId) {
        Date nowDate = new Date();
        String nowStr = DateUtil.formatDate(nowDate);
        //根据当前日期获取周维护数据
        List<MdmWeek> weekList = mdmWeekService.lambdaQuery()
                .le(MdmWeek::getStartDate, nowStr)
                .ge(MdmWeek::getEndDate, nowStr)
                .list();
        if (CollectionUtils.isEmpty(weekList)) {
            throw new JeecgBootException("日期数据异常，提交失败");
        }
        MdmWeek mdmWeek = weekList.get(0);
        //获取当年当周后的周数据
        weekList = mdmWeekService.lambdaQuery()
                .eq(MdmWeek::getYear, mdmWeek.getYear())
                .ge(MdmWeek::getStartDate, mdmWeek.getStartDate())
                .orderByAsc(MdmWeek::getWeekNumber)
                .list();

        String time = meeting.getTime();
        String day = meeting.getDay();
        List<Date> dataList = new ArrayList<>();
        if (meeting.getType() == 1) {
            //日会排期
            //拆分需要排期的周
            String[] dayList = meeting.getDayWeek().split(",");
            //循环所有周数据
            for(MdmWeek weekData : weekList){
                //循环所有需要排期的周
                for(String str : dayList){
                    //判断当前时间是否小于需要排期数
                    Date afterDays = DateUtil.offsetDay(weekData.getStartDate(), weekMap.get(str));
                    if(DateUtil.compare(nowDate, afterDays) < 0){
                        dataList.add(afterDays);
                    }
                }
            }
        } else if (meeting.getType() == 2) {
            //周会排期
            //循环所有周数据
            for(MdmWeek weekData : weekList){
                //判断当前时间是否小于需要排期数
                Date afterDays = DateUtil.offsetDay(weekData.getStartDate(), weekMap.get(day));
                if(DateUtil.compare(nowDate, afterDays) < 0){
                    dataList.add(afterDays);
                }
            }
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            // 获取当前日期
            LocalDate today = LocalDate.now();
            //每月固定周几
            // 获取当前年份
            int currentMonth = DateUtils.getCurrentMonth();
            String currentWeek = meeting.getWeek();
            int week = 0;
            if (StringUtils.isEmpty(currentWeek)) {
                week = Integer.parseInt(currentWeek);
            } else {
                week = Integer.parseInt(currentWeek);
            }
            // 遍历当前年份的每个月
            for (int month = currentMonth; month <= 12; month++) {
                // 获取该月的第一周的第一个星期一（周一为一周的第一天）
                LocalDate date = DateUtils.getDateFromMonthWeekAndDay(today.getYear(), month, week, DayOfWeek.valueOf(day));
                try {
                    dataList.add(format.parse(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        createMeeting(dataList, time, meeting, scheduleId);
    }

    @Override
    public String getMeetingCode(MeetingCheckDto meetingCheckDto) {
        String meetingId = meetingCheckDto.getMeetingId();
        Meeting meeting = this.getById(meetingId);
        //获取对应工厂链接地址
        String factory = meetingCheckDto.getFactoryCode();
        log.error("getMeetingCode factory:" + factory);
        FactoryCodeEnum codeEnum = EnumUtil.getBy(FactoryCodeEnum.class, bean -> bean.getCode().equals(factory));
        String context = codeEnum.getNetWorkAddress() + "mes3/#/mes3/dataCalculation/meeting/calendar/MeetingSignIn/id="
                + meetingId + "/meetingDate=" + DateUtil.date(meeting.getMeetingDate()).toDateStr() + "/factoryName=" + meeting.getMeetingName();
        //生成qr
        QrConfig config = new QrConfig(300, 300);
        // 设置边距，既二维码和背景之间的边距
        config.setMargin(3);
        return QrCodeUtil.generateAsBase64(context, config, ImgUtil.IMAGE_TYPE_JPG);
    }

    /**
     * 封装会议信息和会议模板信息
     *
     * @param dataList
     * @param time
     * @param meeting
     */
    private void createMeeting(List<Date> dataList, String time, MeetingScheduleVO meeting, String scheduleId) {
        if (CollectionUtils.isNotEmpty(dataList)) {
            List<Meeting> meetingList = new ArrayList<>();
            List<MeetingTemplate> list = meetingTemplateService.getMeetingTemplates(meeting.getMeetingType());
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            for (Date date : dataList) {
                if(date.before(new Date())){
                    continue;
                }
                Meeting meetingEntity = new Meeting();
                meetingEntity.setScheduleId(scheduleId);
                meetingEntity.setFactoryCode(list.get(0).getFactoryCode());
                meetingEntity.setDeptCode(list.get(0).getDeptCode());
                meetingEntity.setWorkshop(list.get(0).getWorkshop());
                meetingEntity.setLineCode(list.get(0).getLineCode());
                meetingEntity.setMeetingName(list.get(0).getMeetingName());
                meetingEntity.setMeetingType(list.get(0).getMeetingType());
                meetingEntity.setMeetingDate(date);
                meetingEntity.setMeetingTime(time);
                meetingEntity.setParticipant(list.get(0).getParticipant());
                meetingList.add(meetingEntity);
                if(meetingList.size() >= 500){
                    this.saveBatch(meetingList);
                    meetingList.clear();
                }
            }
            if(CollectionUtil.isNotEmpty(meetingList)){
                this.saveBatch(meetingList);
            }
        }
    }

    @Override
    public void setTemplate(Meeting meeting) {
        List<MeetingRecordTemplate> templateList = meetingRecordTemplateService.list();
        List<String> meetingIds = templateList
                .stream()
                .map(MeetingRecordTemplate::getId)
                .toList();
        List<MeetingRecord> meetingRecordList = meetingRecordService.lambdaQuery()
                .eq(MeetingRecord::getMeetingId, meeting.getId())
                .in(MeetingRecord::getRecordTemplateId, meetingIds)
                .list();
        Map<String, MeetingRecord> meetingRecordMap = meetingRecordList.stream()
                .collect(Collectors.toMap(
                        MeetingRecord::getRecordTemplateId,
                        record -> record,
                        // 当有重复key时保留第一个
                        (existing, replacement) -> existing
                ));
        List<MeetingRecord> saveList = new ArrayList<>();
        List<MeetingRecord> updateList = new ArrayList<>();
        for (MeetingRecordTemplate meetingRecordTemplate : templateList) {
            MeetingRecord record = meetingRecordMap.get(meetingRecordTemplate.getId());
            if(null == record){
                record = new MeetingRecord();
                record.setMeetingId(meeting.getId());
                record.setRecordType(meetingRecordTemplate.getRecordType());
                record.setRecordTemplateId(meetingRecordTemplate.getId());
                record.setMeetingType(meeting.getMeetingType());
                record.setContent(meetingRecordTemplate.getContent());
                record.setStandard(meetingRecordTemplate.getStandard());
                record.setWeight(meetingRecordTemplate.getWeight());
                record.setFactoryCode(meeting.getFactoryCode());
                record.setDeptCode(meeting.getDeptCode());
                record.setLineCode(meeting.getLineCode());
                saveList.add(record);
            } else {
                record.setRecordType(meetingRecordTemplate.getRecordType());
                updateList.add(record);
            }
        }
        if(CollectionUtil.isNotEmpty(saveList)){
            meetingRecordService.saveBatch(saveList);
        }
        if(CollectionUtil.isNotEmpty(updateList)){
            meetingRecordService.updateBatchById(updateList);
        }
    }

    @Override
    public List<MeetingTotalDTO> meetingStatistics(MeetingVO meetingVO) {
        List<MeetingConfig> configList = meetingConfigService.list(new LambdaQueryWrapper<MeetingConfig>()
                .le(MeetingConfig::getMeetingCycle,3));
        List<MeetingTotalDTO> result = new ArrayList<>();
        Map<String,MeetingTotalDTO> deptMap = new HashMap<>();
        //按照频率分组
        Map<Integer, List<MeetingConfig>> groupMap = configList.stream().collect(Collectors.groupingBy(MeetingConfig::getMeetingCycle));
        groupMap.forEach((k, v) -> {
            for (MeetingConfig meetingConfig : v) {
                LambdaQueryWrapper<Meeting> queryWrapper = new LambdaQueryWrapper<Meeting>()
                        .eq(StringUtils.isNotEmpty(meetingConfig.getMeetingType()), Meeting::getMeetingType, meetingConfig.getId());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                if (StringUtils.isNotEmpty(meetingVO.getTimes())) {
                    String[] times = meetingVO.getTimes().split(",");
                    try {
                        queryWrapper.between(Meeting::getMeetingDate, sdf.parse(times[0]), sdf.parse(times[1]));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    //默认查询当前月
                    try {
                        queryWrapper.between(Meeting::getMeetingDate, sdf.parse(DateUtils.getFirstDayOfMonth()), sdf.parse(DateUtils.getLastDayOfMonth()));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                List<Meeting> meetingList = this.list(queryWrapper);
                if (CollectionUtils.isNotEmpty(meetingList)) {
                    //对会议按照部门分组
//                    Map<String, List<Meeting>> deptMap = meetingList.stream().collect(Collectors.groupingBy(Meeting::getDeptCode));
//                    deptMap.forEach((k1, v1) -> {
                    MeetingTotalDTO totalDTO = new MeetingTotalDTO();
                    if(StringUtils.isNotEmpty(meetingConfig.getDeptCode())) {
                        totalDTO.setDept(meetingConfig.getDeptCode());
                    } else if(StringUtils.isNotEmpty(meetingConfig.getWorkshop())) {
                        totalDTO.setDept(meetingConfig.getWorkshop());
                    } else if(StringUtils.isNotEmpty(meetingConfig.getLineCode())) {
                        totalDTO.setDept(meetingConfig.getLineCode());
                    } else {
                        totalDTO.setDept(meetingConfig.getFactoryCode());
                    }
                    //会议总数
                    int total = meetingList.size();
                    if(k == 1){
                        totalDTO.setAll_count_day(total);
                        totalDTO.setPlan_count_day(total);
                    } else if(k == 2){
                        totalDTO.setAll_count_week(total);
                        totalDTO.setPlan_count_week(total);
                    } else if(k == 3){
                        totalDTO.setAll_count_month(total);
                        totalDTO.setPlan_count_month(total);
                    }
                    for (Meeting meeting : meetingList) {
                        //会议取消数
                        if(meeting.getActive() == 2){
                            if(k == 1) {
                                totalDTO.setCanceled_count_day(totalDTO.getCanceled_count_day() + 1);
                            } else if(k == 2) {
                                totalDTO.setCanceled_count_week(totalDTO.getCanceled_count_week() + 1);
                            } else if(k == 3) {
                                totalDTO.setCanceled_count_month(totalDTO.getCanceled_count_month() + 1);
                            }
                        }
                        if(meeting.getActive() == 1) {
                            long count = meetingCheckService.count(new LambdaQueryWrapper<MeetingCheck>().eq(MeetingCheck::getMeetingId, meeting.getId())
                                    .eq(MeetingCheck::getActive, 1));
                            //会议召开数
                            if (count > 0) {
                                if (k == 1) {
                                    totalDTO.setOpen_count_day(totalDTO.getOpen_count_day() + 1);
                                } else if (k == 2) {
                                    totalDTO.setOpen_count_week(totalDTO.getOpen_count_week() + 1);
                                } else if (k == 3) {
                                    totalDTO.setOpen_count_month(totalDTO.getOpen_count_month() + 1);
                                }
                            }
                        }

                        List<MeetingRecord> meetingRecordList = meetingRecordService.list(new LambdaQueryWrapper<MeetingRecord>().eq(MeetingRecord::getMeetingId, meeting.getId()));
                        if (CollectionUtils.isNotEmpty(meetingRecordList)) {
                            double score = 0;
                            for (MeetingRecord meetingRecord : meetingRecordList) {
                                if (null != meetingRecord.getScore()) {
                                    score += meetingRecord.getScore();
                                }
                            }
                            //会议评估数
                            if(score > 0){
                                if(k == 1) {
                                    totalDTO.setRecord_count_day(totalDTO.getRecord_count_day() + 1);
                                } else if(k == 2) {
                                    totalDTO.setRecord_count_week(totalDTO.getRecord_count_week() + 1);
                                } else if(k == 3) {
                                    totalDTO.setRecord_count_month(totalDTO.getRecord_count_month() + 1);
                                }
                            }
                        }
                    }
                    if(deptMap.containsKey(totalDTO.getDept())){
                        MeetingTotalDTO meetingTotalDTO = deptMap.get(totalDTO.getDept());
                        if(null != meetingTotalDTO){
                            try {
                                ObjectMerger.mergeNonNullProperties(meetingTotalDTO, totalDTO);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                            deptMap.put(totalDTO.getDept(), totalDTO);
                        }
                    } else {
                        if (StringUtils.isNotEmpty(meetingConfig.getDeptCode())) {
                            deptMap.put(meetingConfig.getDeptCode(), totalDTO);
                        } else if (StringUtils.isNotEmpty(meetingConfig.getWorkshop())) {
                            deptMap.put(meetingConfig.getWorkshop(), totalDTO);
                        } else if (StringUtils.isNotEmpty(meetingConfig.getLineCode())) {
                            deptMap.put(meetingConfig.getLineCode(), totalDTO);
                        } else {
                            deptMap.put(meetingConfig.getFactoryCode(), totalDTO);
                        }
                    }
                }
            }
        });
        //判断如果存在车间或者产线会议，都归属到部门下
        // 使用迭代器安全删除
        Iterator<Map.Entry<String, MeetingTotalDTO>> iterator = deptMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, MeetingTotalDTO> entry = iterator.next();
            String k = entry.getKey();
            MeetingTotalDTO v = entry.getValue();
            if(k.length() > 12){
                String deptCode = k.substring(0,12);
                MeetingTotalDTO totalDTO = deptMap.get(deptCode);
                if(null != totalDTO){
                    totalDTO.setOpen_count_week(v.getOpen_count_week() + totalDTO.getOpen_count_week());
                    totalDTO.setOpen_count_month(v.getOpen_count_month() + totalDTO.getOpen_count_month());
                    totalDTO.setOpen_count_day(v.getOpen_count_day() + totalDTO.getOpen_count_day());
                    totalDTO.setPlan_count_day(v.getPlan_count_day() + totalDTO.getPlan_count_day());
                    totalDTO.setPlan_count_week(v.getPlan_count_week() + totalDTO.getPlan_count_week());
                    totalDTO.setPlan_count_month(v.getPlan_count_month() + totalDTO.getPlan_count_month());
                    totalDTO.setRecord_count_week(v.getRecord_count_week() + totalDTO.getRecord_count_week());
                    totalDTO.setRecord_count_month(v.getRecord_count_month() + totalDTO.getRecord_count_month());
                    totalDTO.setRecord_count_day(v.getRecord_count_day() + totalDTO.getRecord_count_day());
                    totalDTO.setCanceled_count_day(v.getCanceled_count_day() + totalDTO.getCanceled_count_day());
                    totalDTO.setCanceled_count_week(v.getCanceled_count_week() + totalDTO.getCanceled_count_week());
                    totalDTO.setCanceled_count_month(v.getCanceled_count_month() + totalDTO.getCanceled_count_month());
                    totalDTO.setAll_count_week(v.getAll_count_week() + totalDTO.getAll_count_week());
                    totalDTO.setAll_count_month(v.getAll_count_month() + totalDTO.getAll_count_month());
                    totalDTO.setAll_count_day(v.getAll_count_day() + totalDTO.getAll_count_day());
                    deptMap.put(deptCode, totalDTO);
                    iterator.remove();
                }
            }
        }
        deptMap.forEach((k, v) -> {

            SysDepart sysDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>()
                    .eq(SysDepart::getOrgCode, k));
            if(null != sysDepart){
                v.setDept(sysDepart.getDepartName());
                if(v.getOpen_count_day() != 0){
                    if(v.getAll_count_day() == v.getCanceled_count_day()) {
                        v.setRange_count_day("0.00%");
                    } else {
                        double value = (double) v.getOpen_count_day() / (double) (v.getAll_count_day() - v.getCanceled_count_day());
                        BigDecimal rate = new BigDecimal(Double.toString(value * 100));
                        rate = rate.setScale(2, RoundingMode.HALF_UP);
                        v.setRange_count_day(rate + "%");
                    }
                } else {
                    v.setRange_count_day("0");
                }
                if(v.getOpen_count_week() != 0){
                    if(v.getAll_count_week() == v.getCanceled_count_week()){
                        v.setRange_count_week("0.00%");
                    } else {
                        double value = (double) v.getOpen_count_week() / (double) (v.getAll_count_week() - v.getCanceled_count_week());
                        BigDecimal rate = new BigDecimal(Double.toString(value * 100));
                        rate = rate.setScale(2, RoundingMode.HALF_UP);
                        v.setRange_count_week(rate + "%");
                    }
                } else {
                    v.setRange_count_week("0");
                }
                if(v.getOpen_count_month() != 0){
                    if(v.getAll_count_month() == v.getCanceled_count_month()){
                        v.setRange_count_month("0.00%");
                    } else {
                        double value = (double) v.getOpen_count_month() / (double) (v.getAll_count_month() - v.getCanceled_count_month());
                        BigDecimal rate = new BigDecimal(Double.toString(value * 100));
                        rate = rate.setScale(2, RoundingMode.HALF_UP);
                        v.setRange_count_month(rate + "%");
                    }
                } else {
                    v.setRange_count_month("0");
                }
                result.add(v);
            }
        });
        result.sort(Comparator
                .comparing(MeetingTotalDTO::getDept));
        return result;
    }
}

