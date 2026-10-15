package com.keytech.modules.period.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.common.NumberCalcUtil;
import com.keytech.modules.cop.dto.CopPlanRank;
import com.keytech.modules.cop.dto.CopScheduleJson;
import com.keytech.modules.cop.dto.CopSchedulerVo;
import com.keytech.modules.cop.entity.CopPoint;
import com.keytech.modules.cop.entity.CopSchedule;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import com.keytech.modules.period.dto.PeriodScheduleVO;
import com.keytech.modules.period.dto.PeriodStatisticsDto;
import com.keytech.modules.period.dto.ScheduleJson;
import com.keytech.modules.period.entity.PeriodVerificationDevice;
import com.keytech.modules.period.entity.PeriodVerificationSchedule;
import com.keytech.modules.period.entity.PeriodVerificationSubmit;
import com.keytech.modules.period.mapper.PeriodVerificationScheduleMapper;
import com.keytech.modules.period.service.IPeriodVerificationDeviceService;
import com.keytech.modules.period.service.IPeriodVerificationScheduleService;
import com.keytech.modules.period.service.IPeriodVerificationSubmitService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 期间核查排期
 * @Author: qipengbo
 * @Date:   2025-03-11
 * @Version: V1.0
 */
@Service
public class PeriodVerificationScheduleServiceImpl extends BaseServiceImpl<PeriodVerificationScheduleMapper, PeriodVerificationSchedule> implements IPeriodVerificationScheduleService {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM.dd");


    @Autowired
    private IPeriodVerificationDeviceService periodVerificationDeviceService;

    @Autowired
    private IMdmWeekService mdmWeekService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysDepartService sysDepartService;

    @Autowired
    private IPeriodVerificationDeviceService deviceService;

    @Autowired
    private IPeriodVerificationSubmitService periodVerificationSubmitService;

    @Override
    public Page<PeriodVerificationSchedule> findPage(PeriodVerificationSchedule periodScheduleVO, List<String> months, Integer pageNo, Integer pageSize) {
        Page<PeriodVerificationSchedule> page = new Page<>(pageNo, pageSize);
        List<PeriodVerificationSchedule> list = new ArrayList<>();
        //1.查询出所有设备列表
        Page<PeriodVerificationDevice> devicePage = new Page<>(pageNo, pageSize);
        if(periodScheduleVO.getFactoryCode().equals(periodScheduleVO.getDeptCode())){
            periodScheduleVO.setDeptCode(null);
        }
        Page<PeriodVerificationDevice> deviceListPage = periodVerificationDeviceService.page(devicePage, new LambdaQueryWrapper<PeriodVerificationDevice>()
                .eq(StringUtils.isNotBlank(periodScheduleVO.getDeptCode()), PeriodVerificationDevice::getDeptCode, periodScheduleVO.getDeptCode())
                .eq(StringUtils.isNotBlank(periodScheduleVO.getLineCode()), PeriodVerificationDevice::getLineCode, periodScheduleVO.getLineCode())
                .like(StringUtils.isNotBlank(periodScheduleVO.getApparatusName()), PeriodVerificationDevice::getApparatusName, "%" + periodScheduleVO.getApparatusName() + "%")
                .eq(StringUtils.isNotBlank(periodScheduleVO.getFrequency()), PeriodVerificationDevice::getFrequencyOfCheck, periodScheduleVO.getFrequency())
                .eq(StringUtils.isNotEmpty(periodScheduleVO.getCheckUser()), PeriodVerificationDevice::getCheckUser, periodScheduleVO.getCheckUser())
                .eq(PeriodVerificationDevice::getActive,1)
                .eq(StrUtil.isNotBlank(periodScheduleVO.getApparatusId()), PeriodVerificationDevice::getId, periodScheduleVO.getApparatusId())
                .orderByAsc(PeriodVerificationDevice::getSort)
        );
        List<PeriodVerificationDevice> deviceList = deviceListPage.getRecords();
        if(CollectionUtils.isNotEmpty(deviceList)) {
            //2.查询现有排期表中对应查询条件的排期数据
//            List<String> deviceIds = deviceList.stream().map(PeriodVerificationDevice::getId).toList();
            page.setTotal(deviceListPage.getTotal());
            page.setCurrent(deviceListPage.getCurrent());
//            List<PeriodVerificationSchedule> scheduleList = this.list(new LambdaQueryWrapper<PeriodVerificationSchedule>()
//                    .in( PeriodVerificationSchedule::getApparatusId, deviceIds)
//                    .eq(PeriodVerificationSchedule::getPlanYear, periodScheduleVO.getPlanYear())
//            );
            // List<PeriodVerificationSchedule> scheduleList = schedules.getRecords();
            //3.对比查询结果，如果所有设备都有排期数据则直接返回排期结果，
            List<MdmWeek> weeks = new ArrayList<>();
            List<ScheduleJson> weeksJson = new ArrayList<>();
            populateWeeks(months, weeks, periodScheduleVO);
            weeks.forEach(week -> weeksJson.add(createScheduleJson(week)));
            //3.1 判断查询出的排期表中的数据是否为空，如果为空则说明该设备没有排期数据，需要构造空的排期数据并追加到现有排期数据中，返回排期结果。

            // if (scheduleList.isEmpty()) {
            //将构造好的空排期数据追加到设备中
            for (PeriodVerificationDevice device : deviceList) {
                PeriodVerificationSchedule scheduleDto = new PeriodVerificationSchedule();
                BeanUtils.copyProperties(device, scheduleDto);
                scheduleDto.setApparatusId(device.getId());
                scheduleDto.setFrequency(device.getFrequencyOfCheck());
                PeriodVerificationSchedule schedule = this.getOne(new LambdaQueryWrapper<PeriodVerificationSchedule>().eq(PeriodVerificationSchedule::getApparatusId, device.getId()).eq(PeriodVerificationSchedule::getPlanYear, periodScheduleVO.getPlanYear()));
                if (null != schedule) {
                    scheduleDto.setId(schedule.getId());
                    String weekJson = schedule.getWeekJson();
                    if (StringUtils.isNotBlank(weekJson)) {
                        List<ScheduleJson> scheduleJsons = JSONArray.parseArray(weekJson, ScheduleJson.class);
                        scheduleJsons = scheduleJsons.stream()
                                .sorted(Comparator.comparing(ScheduleJson::getNum)).toList();
                        //排除当前选择的月份其他的数据
                        if (months.size() < 12) {
                            scheduleJsons = new ArrayList<>(scheduleJsons);
                            scheduleJsons.removeIf(copScheduleJson -> !months.contains(String.valueOf(copScheduleJson.getMonth())));
                        }
                        scheduleDto.setPlanYear(schedule.getPlanYear());
                        scheduleDto.setWeekJson(JSONArray.toJSONString(scheduleJsons));
                    } else {
                        scheduleDto.setWeekJson(JSONArray.toJSONString(weeksJson));
                    }
                    list.add(scheduleDto);
                } else {
                    scheduleDto.setWeekJson(JSONArray.toJSONString(weeksJson));
                    scheduleDto.setPlanYear(periodScheduleVO.getPlanYear());
                    list.add(scheduleDto);
                }
            }
        }
        return page.setRecords(list);
    }

    @Override
    public PeriodVerificationSchedule handlePeriodSchedule(PeriodScheduleVO periodScheduleVO) {
        //1.判断是否存在该排期数据
        PeriodVerificationSchedule oldSchedule = this.getOne(new LambdaQueryWrapper<PeriodVerificationSchedule>()
                .eq(StringUtils.isNotBlank(periodScheduleVO.getId()), PeriodVerificationSchedule::getId, periodScheduleVO.getId())
                .eq(PeriodVerificationSchedule::getApparatusId, periodScheduleVO.getApparatusId())
                .eq(PeriodVerificationSchedule::getPlanYear, periodScheduleVO.getPlanYear()));
        String frequency = periodScheduleVO.getFrequency();
        List<ScheduleJson> scheduleJsons;
        if(null != oldSchedule){
            // 2.如果存在，则更新排期数据
            scheduleJsons = JSONArray.parseArray(oldSchedule.getWeekJson(), ScheduleJson.class);
            scheduleJsons.stream()
                    .sorted(Comparator.comparing(ScheduleJson::getNum))
                    .toList();
            // 2.1 需要更新当前提交的周数的排期
            // 2.2首先清空当前提交的周数之后的排期数据
            boolean flag = false;
            for (ScheduleJson json : scheduleJsons) {
                handleJson(json, periodScheduleVO);
                if (json.getNum() >= periodScheduleVO.getNum() && StringUtils.isNotEmpty(json.getState())) {
                    flag = true;
                }
            }
            if(flag){
                scheduleJsons.forEach(scheduleJson -> {
                    handleJson(scheduleJson, periodScheduleVO);
                    if (scheduleJson.getNum() == periodScheduleVO.getNum()) {
                        if (periodScheduleVO.getResult() == 1) {
                            scheduleJson.setState("-1");
                        } else if (periodScheduleVO.getResult() == 2) {
                            scheduleJson.setState("6");
                        } else if (periodScheduleVO.getResult() == 7) {
                            scheduleJson.setState("7");
                        } else {
                            scheduleJson.setState("");
                        }
                    }
                });
            } else {
                scheduleJsons.forEach(scheduleJson -> {
                    handleJson(scheduleJson, periodScheduleVO);
                    if (scheduleJson.getNum() == periodScheduleVO.getNum()) {
                        if (periodScheduleVO.getResult() == 1) {
                            scheduleJson.setState("-1");
                        } else if (periodScheduleVO.getResult() == 2) {
                            scheduleJson.setState("6");
                        } else if (periodScheduleVO.getResult() == 7) {
                            scheduleJson.setState("7");
                        } else {
                            scheduleJson.setState("");
                        }
                        //上个版本的逻辑，如果存在排期，重新排期的话，需要先清空当前周后原有排期
                        //现有逻辑，如果已有排期则只排当前周的，其他不变
                    } else if (scheduleJson.getNum() > periodScheduleVO.getNum()) {
                        scheduleJson.setState("");
                    }
                });
                // 2.3 然后更新当前提交的周数的排期数据
                handleSchedule(scheduleJsons, periodScheduleVO, frequency);
            }
            oldSchedule.setWeekJson(JSONArray.toJSONString(scheduleJsons));
            this.updateById(oldSchedule);
        } else {
            // 3.如果不存在，则新增排期数据
            oldSchedule = new PeriodVerificationSchedule();
            oldSchedule.setFrequency(frequency);
            oldSchedule.setApparatusId(periodScheduleVO.getApparatusId());

            oldSchedule.setCheckUser(periodScheduleVO.getCheckUser());
            oldSchedule.setVerifyUser(periodScheduleVO.getVerifyUser());
            PeriodVerificationDevice device = periodVerificationDeviceService.getOne(new LambdaQueryWrapper<PeriodVerificationDevice>()
                    .eq(PeriodVerificationDevice::getId, periodScheduleVO.getApparatusId()));
            BeanUtils.copyProperties(device, oldSchedule);
            oldSchedule.setId(null);
            oldSchedule.setPlanYear(Integer.valueOf(periodScheduleVO.getPlanYear()));
            // 3.1 首先构造空的排期数据
            //如果没有数据，用周接口获取一个模板
            MdmWeek mdmWeek = new MdmWeek();
            mdmWeek.setYear(String.valueOf(periodScheduleVO.getPlanYear()));
            List<MdmWeek> weekList = mdmWeekService.getWeek(mdmWeek);
            scheduleJsons = new ArrayList<>();
            for (MdmWeek week : weekList) {
                ScheduleJson newScheduleJson = new ScheduleJson();
                newScheduleJson.setYear(Integer.valueOf(week.getYear()));
                newScheduleJson.setMonth(Integer.valueOf(week.getMonth()));
                newScheduleJson.setNum(week.getWeekNumber());
                newScheduleJson.setData(SIMPLE_DATE_FORMAT.format(week.getStartDate()) + "-" + SIMPLE_DATE_FORMAT.format(week.getEndDate()));
                newScheduleJson.setState("");
                handleJson(newScheduleJson, periodScheduleVO);
                //3.6 如果是一年或者以上的，只排当前提交周的
                scheduleJsons.add(newScheduleJson);
            }
            for (ScheduleJson scheduleJson : scheduleJsons) {
                //先处理当前提交的周
                if (scheduleJson.getNum() == periodScheduleVO.getNum()) {
                    if (periodScheduleVO.getResult() == 1) {
                        scheduleJson.setState("-1");
                    } else if (periodScheduleVO.getResult() == 7) {
                        scheduleJson.setState("7");
                    } else {
                        scheduleJson.setState("6");
                    }
                } else {
                    scheduleJson.setState("");
                }
                handleSchedule(scheduleJsons, periodScheduleVO, frequency);
            }
            oldSchedule.setWeekJson(JSONArray.toJSONString(scheduleJsons));
            this.save(oldSchedule);
        }
        // 4.返回排期数据
        return oldSchedule;
    }

    @Override
    public PeriodVerificationSchedule handleCancelPeriodSchedule(PeriodScheduleVO periodScheduleVO) {
        //1.判断是否存在该排期数据
        PeriodVerificationSchedule oldSchedule = this.getOne(new LambdaQueryWrapper<PeriodVerificationSchedule>()
                .eq(StringUtils.isNotBlank(periodScheduleVO.getId()), PeriodVerificationSchedule::getId, periodScheduleVO.getId())
                .eq(PeriodVerificationSchedule::getApparatusId, periodScheduleVO.getApparatusId())
                .eq(PeriodVerificationSchedule::getPlanYear, periodScheduleVO.getPlanYear()));
        List<ScheduleJson> scheduleJsons;
        if(null != oldSchedule){
            // 2.如果存在，则更新排期数据
            scheduleJsons = JSONArray.parseArray(oldSchedule.getWeekJson(), ScheduleJson.class);
            scheduleJsons.stream()
                    .sorted(Comparator.comparing(ScheduleJson::getNum))
                    .toList();
            // 2.1 需要更新当前提交的周数的排期
            // 2.2首先清空当前提交的周数之后的排期数据
            scheduleJsons.forEach(scheduleJson -> {
                handleJson(scheduleJson, periodScheduleVO);
                //上个版本的逻辑，如果存在排期，重新排期的话，需要先清空当前周后原有排期
                //现有逻辑，如果已有排期则只排当前周的，其他不变
                if(periodScheduleVO.getNum() == 0){
                    throw new JeecgBootException("对应周错误");
                }
                if(periodScheduleVO.getType() == 0){
                    if (scheduleJson.getNum() == periodScheduleVO.getNum()) {
                        scheduleJson.setState("");
                    }
                } else {
                    if (scheduleJson.getNum() >= periodScheduleVO.getNum()) {
                        scheduleJson.setState("");
                    }
                }
            });
            // 2.3 然后更新当前提交的周数的排期数据
            //handleSchedule(scheduleJsons, periodScheduleVO, frequency);
            oldSchedule.setWeekJson(JSONArray.toJSONString(scheduleJsons));
            this.updateById(oldSchedule);
        }
        return oldSchedule;
    }

    @Override
    public PeriodVerificationSchedule processPeriodSchedule(String id, int num, String status) {
        PeriodVerificationSchedule schedule = this.getById(id);
        if(null != schedule){
            List<ScheduleJson> weekList = JSONArray.parseArray(schedule.getWeekJson(), ScheduleJson.class);
            if(CollectionUtils.isNotEmpty(weekList)) {
                weekList.forEach(scheduleJson -> {
                    if (scheduleJson.getNum() == num) {
                        scheduleJson.setState(status);
                    }
                });
            }
            schedule.setWeekJson(JSONArray.toJSONString(weekList));
        }
        this.updateById(schedule);
//        if("-1".equals(status)) {
//            periodVerificationSubmitService.remove(new LambdaQueryWrapper<PeriodVerificationSubmit>().eq(PeriodVerificationSubmit::getPeriodScheduleId, id));
//        }
        return schedule;
    }

    @Override
    public void updatePeriod(List<PeriodScheduleVO> periodScheduleVO) throws Exception {
        for (PeriodScheduleVO scheduleVo : periodScheduleVO) {
            PeriodVerificationSchedule schedule = this.getById(scheduleVo.getId());
            PeriodVerificationDevice device = null;
            if(null != schedule) {
                device = deviceService.getById(schedule.getApparatusId());
                if(StringUtils.isNotEmpty(scheduleVo.getFrequency())) {
                    schedule.setFrequency(scheduleVo.getFrequency());
                    device.setFrequencyOfCheck(scheduleVo.getFrequency());
                }
                if(StringUtils.isNotEmpty(scheduleVo.getCheckUser())) {
                    schedule.setCheckUser(scheduleVo.getCheckUser());
                    device.setCheckUser(scheduleVo.getCheckUser());
                }
                if(StringUtils.isNotEmpty(scheduleVo.getVerifyUser())) {
                    schedule.setVerifyUser(scheduleVo.getVerifyUser());
                    device.setVerifyUser(scheduleVo.getVerifyUser());
                }
                this.updateById(schedule);
            } else {
                device = deviceService.getById(scheduleVo.getId());
                if(StringUtils.isNotEmpty(scheduleVo.getFrequency())) {
                    device.setFrequencyOfCheck(scheduleVo.getFrequency());
                }
                if(StringUtils.isNotEmpty(scheduleVo.getCheckUser())) {
                    device.setCheckUser(scheduleVo.getCheckUser());
                }
                if(StringUtils.isNotEmpty(scheduleVo.getVerifyUser())) {
                    device.setVerifyUser(scheduleVo.getVerifyUser());
                }
            }
            deviceService.updateById(device);
        }
    }

    @Override
    public List<PeriodStatisticsDto> periodStatistics(PeriodStatisticsDto periodStatisticsDto) {
        //test2
        List<PeriodStatisticsDto> result = new ArrayList<>();
        LambdaQueryWrapper<PeriodVerificationSchedule> queryWrapper = new LambdaQueryWrapper<PeriodVerificationSchedule>()
                .select(PeriodVerificationSchedule::getId, PeriodVerificationSchedule::getPlanYear, PeriodVerificationSchedule::getApparatusId, PeriodVerificationSchedule::getApparatusName,  PeriodVerificationSchedule::getWeekJson, PeriodVerificationSchedule::getCheckUser, PeriodVerificationSchedule::getVerifyUser)
                .eq(StringUtils.isNotBlank(periodStatisticsDto.getDeptCode()), PeriodVerificationSchedule::getDeptCode, periodStatisticsDto.getDeptCode())
                .eq(StringUtils.isNotBlank(periodStatisticsDto.getLineCode()), PeriodVerificationSchedule::getLineCode, periodStatisticsDto.getLineCode())
                .eq(PeriodVerificationSchedule::getPlanYear, periodStatisticsDto.getYear());
        if(StringUtils.isNotEmpty(periodStatisticsDto.getFrequencys())){
            queryWrapper.in(PeriodVerificationSchedule::getFrequency, Arrays.asList(periodStatisticsDto.getFrequencys().split(",")));
        }

        List<PeriodVerificationSchedule> periodSchedules = this.list(queryWrapper);
        List<String> monthList;
        if(StringUtils.isNotBlank(periodStatisticsDto.getMonths())) {
            monthList = Arrays.asList(periodStatisticsDto.getMonths().split(","));
        } else {
            monthList = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12");
        }
        if (CollectionUtils.isNotEmpty(periodSchedules)) {
            //获取用户信息
            List<SysUser> userList = userService.list();
            //将用户信息按照id分组
            Map<String, SysUser> userMap = userList.stream()
                    .collect(Collectors.toMap(
                            SysUser::getId,
                            item -> item,
                            // 如果 key 重复，保留第一个
                            (existing, replacement) -> existing
                    ));

            //获取部门信息
            List<SysDepart> deptList = sysDepartService.list();
            //将部门信息按照Code分组
            Map<String, SysDepart> deptMap = deptList.stream()
                    .collect(Collectors.toMap(
                            SysDepart::getOrgCode,
                            item -> item,
                            // 如果 key 重复，保留第一个
                            (existing, replacement) -> existing
                    ));

            if(periodStatisticsDto.getType() == 0) {
                //3.1 构造每个设备的清洗点清洗计划
                Map<String, List<PeriodVerificationSchedule>> deviceMap = new HashMap<>();
                for (PeriodVerificationSchedule schedule : periodSchedules) {
                    deviceMap.computeIfAbsent(schedule.getApparatusId(), k -> new ArrayList<>()).add(schedule);
                }
                //设备信息
                List<String> deviceIds = new ArrayList<>(deviceMap.keySet());
                Map<String, PeriodVerificationDevice> deviceCache = deviceService.listByIds(deviceIds)
                        .stream()
                        .collect(Collectors.toMap(
                                PeriodVerificationDevice::getId,
                                item -> item,
                                (existing, replacement) -> existing
                        ));

                deviceMap.forEach((deviceId, schedules) -> {
                    PeriodVerificationDevice device = deviceCache.get(deviceId);
                    if(null != device) {
                        PeriodStatisticsDto statisticsDto = createPeriodStatisticsDto(
                                device.getApparatusName(), periodStatisticsDto, schedules, monthList, deptMap
                        );
                        result.add(statisticsDto);
                    }
                });
            }
            if(periodStatisticsDto.getType() == 1 || periodStatisticsDto.getType() == 2) {
                //获取所有id
                List<String> periodScheduleIds = periodSchedules.stream()
                        .map(PeriodVerificationSchedule::getId)
                        .toList();
                //获取提交记录
                List<PeriodVerificationSubmit> submitList = periodVerificationSubmitService.lambdaQuery()
                        .in(PeriodVerificationSubmit::getPeriodScheduleId, periodScheduleIds)
                        .eq(PeriodVerificationSubmit::getPlanYear, periodStatisticsDto.getYear())
                        .in(PeriodVerificationSubmit::getPlanMonth, monthList)
                        .list();
                Map<String, PeriodVerificationSubmit> submitMap = submitList.stream()
                        .collect(Collectors.toMap(
                                item -> item.getPeriodScheduleId() + "_" + item.getPlanYear() + "_" +
                                        item.getPlanMonth() + "_" + item.getPlanWeek(),
                                item -> item,
                                // 如果 key 重复，保留第一个
                                (existing, replacement) -> existing
                        ));
                //临时结果存储，用户id为key
                Map<String, PeriodStatisticsDto> tempResultMap = new HashMap<>();
                for (PeriodVerificationSchedule schedule : periodSchedules) {
                    List<ScheduleJson> list = JSONArray.parseArray(schedule.getWeekJson(), ScheduleJson.class);
                    list.removeIf(scheduleJson -> !monthList.contains(String.valueOf(scheduleJson.getMonth())));
                    for (ScheduleJson json : list) {
                        //获取提交记录
                        String submitKey = schedule.getId() + "_" + json.getYear() + "_" +
                                json.getMonth() + "_" + json.getNum();
                        PeriodVerificationSubmit periodVerificationSubmit = submitMap.get(submitKey);
                        //获取临时结果key
                        String resultKey;
                        if(periodStatisticsDto.getType() == 1){
                            resultKey = null == periodVerificationSubmit ? schedule.getCheckUser() : periodVerificationSubmit.getCheckUser();
                        }else{
                            resultKey = null == periodVerificationSubmit ? schedule.getVerifyUser() : periodVerificationSubmit.getVerifyUser();
                        }
                        //获取结果对象
                        PeriodStatisticsDto statisticsDto;
                        if(null == tempResultMap.get(resultKey)){
                            statisticsDto = new PeriodStatisticsDto();
                            statisticsDto.setPlan(0);
                            statisticsDto.setComplete(0);
                            statisticsDto.setDelay(0);
                            statisticsDto.setDelayComplete(0);
                            statisticsDto.setCheck(0);
                            statisticsDto.setNoExecute(0);
                            statisticsDto.setNoExecutePlan(0);
                            statisticsDto.setUnqualified(0);
                            statisticsDto.setName(null == userMap.get(resultKey) ? null : userMap.get(resultKey).getRealname());
                            getDeptName(periodStatisticsDto, deptMap, statisticsDto);
                        } else {
                            statisticsDto = tempResultMap.get(resultKey);
                        }
                        //根据排期统计数据
                        switch (json.getState()) {
                            case "-1": {
                                //计划
                                statisticsDto.setPlan(statisticsDto.getPlan() + 1);
                                break;
                            }
                            case "0": {
                                statisticsDto.setUnqualified(statisticsDto.getUnqualified() + 1);
                                break;
                            }
                            case "1": {
                                statisticsDto.setComplete(statisticsDto.getComplete() + 1);
                                break;
                            }
                            case "2": {
                                statisticsDto.setDelay(statisticsDto.getDelay() + 1);
                                break;
                            }
                            case "3": {
                                statisticsDto.setDelayComplete(statisticsDto.getDelayComplete() + 1);
                                break;
                            }
                            case "4": {
                                statisticsDto.setCheck(statisticsDto.getCheck() + 1);
                                break;
                            }
                            case "5": {
                                statisticsDto.setNoExecute(statisticsDto.getNoExecute() + 1);
                                break;
                            }
                            case "6": {
                                statisticsDto.setNoExecutePlan(statisticsDto.getNoExecutePlan() + 1);
                                break;
                            }
                        }
                        processStatistics(statisticsDto);
                        tempResultMap.put(resultKey, statisticsDto);
                    }
                }
                tempResultMap.forEach((key, value) -> {
                    result.add(value);
                });
            }
        }
        return result;
    }

    private void getDeptName(PeriodStatisticsDto periodStatisticsDto, Map<String, SysDepart> deptMap, PeriodStatisticsDto statisticsDto) {
        SysDepart dept = deptMap.get(periodStatisticsDto.getDeptCode());
        if(null != dept) {
            statisticsDto.setDeptCode(dept.getDepartName());
        }
        SysDepart line = deptMap.get(periodStatisticsDto.getLineCode());
        if(null != line) {
            statisticsDto.setLineCode(line.getDepartName());
        }
    }

    @Override
    public List<CopPlanRank> periodPlanRank(WSActionStatisticsDto periodStatisticsDto) throws Exception {
        List<CopPlanRank> result = new ArrayList<>();
        //1.获取当前所有排期数据
        LambdaQueryWrapper<PeriodVerificationSchedule> queryWrapper = new LambdaQueryWrapper<PeriodVerificationSchedule>()
                .select(PeriodVerificationSchedule::getId, PeriodVerificationSchedule::getPlanYear, PeriodVerificationSchedule::getApparatusId, PeriodVerificationSchedule::getApparatusName, PeriodVerificationSchedule::getDeptCode, PeriodVerificationSchedule::getWeekJson, PeriodVerificationSchedule::getCheckUser, PeriodVerificationSchedule::getVerifyUser)
                .eq(PeriodVerificationSchedule::getPlanYear, DateUtils.getCurrentYear());
        List<PeriodVerificationSchedule> periodSchedules = this.list(queryWrapper);
        List<String> monthList = new ArrayList<>();;

        //2.筛选每个设备下所有清洗点排期周期内容的清洗计划
        if(periodStatisticsDto.getDateType() == 1){
            monthList.add(String.valueOf(DateUtils.getCurrentOfWeek() - 1));
        } else if(periodStatisticsDto.getDateType() == 2){
            int month = DateUtils.getCurrentMonth() - 1 ;
            processWeekAndAddToMonthList(month, monthList);
        } else if(periodStatisticsDto.getDateType() == 3){
            int month = DateUtils.getCurrentMonth();
            processWeekAndAddToMonthList(month, monthList);
        }
        
        //3.1 构造每个设备的清洗点计划
        Map<String, List<PeriodVerificationSchedule>> deviceMap = new HashMap<>();
        for (PeriodVerificationSchedule schedule : periodSchedules) {
            deviceMap.computeIfAbsent(schedule.getDeptCode(), k -> new ArrayList<>()).add(schedule);
        }
        
        //3.2 获取所有部门，过滤出部门代码长度为12位的部门
        List<SysDepart> allDepartments = sysDepartService.list(new LambdaQueryWrapper<SysDepart>()
                .eq(SysDepart::getDelFlag, "0"));
        
        // 过滤出部门代码长度为12位的部门
        allDepartments = allDepartments.stream()
                .filter(dept -> dept.getOrgCode() != null && dept.getOrgCode().length() == 12)
                .collect(Collectors.toList());
        
        //3.3 为每个部门创建统计数据，没有数据的部门显示0
        for (SysDepart department : allDepartments) {
            List<PeriodVerificationSchedule> departmentSchedules = deviceMap.getOrDefault(department.getOrgCode(), new ArrayList<>());
            CopPlanRank statisticsDto = createWeekStatisticsDto(department.getDepartName(), departmentSchedules, monthList);
            result.add(statisticsDto);
        }
        
        return result;
    }

    @Override
    public List<CopPlanRank> periodLineStatistics(WSActionStatisticsDto periodStatisticsDto) throws Exception {
        List<CopPlanRank> result = new ArrayList<>();//test1
        //1.获取当前所有排期数据
        LambdaQueryWrapper<PeriodVerificationSchedule> queryWrapper = new LambdaQueryWrapper<PeriodVerificationSchedule>()
                .select(PeriodVerificationSchedule::getId, PeriodVerificationSchedule::getPlanYear, PeriodVerificationSchedule::getApparatusId, PeriodVerificationSchedule::getApparatusName,  PeriodVerificationSchedule::getWeekJson, PeriodVerificationSchedule::getCheckUser, PeriodVerificationSchedule::getVerifyUser)
                .eq(PeriodVerificationSchedule::getPlanYear, DateUtils.getCurrentYear()).eq(PeriodVerificationSchedule::getDeptCode, periodStatisticsDto.getWorkShopCode());
        List<PeriodVerificationSchedule> periodSchedules = this.list(queryWrapper);

        List<String> monthList = new ArrayList<>();;

        //2.筛选每个设备下所有清洗点排期周期内容的清洗计划
        if(periodStatisticsDto.getDateType() == 1){
            monthList.add(String.valueOf(DateUtils.getCurrentOfWeek() - 1));
        } else if(periodStatisticsDto.getDateType() == 2){
            int month = DateUtils.getCurrentMonth() - 1 ;
            processWeekAndAddToMonthList(month, monthList);
        } else if(periodStatisticsDto.getDateType() == 3){
            int month = DateUtils.getCurrentMonth();
            monthList.add(String.valueOf(month));
        }
        if (CollectionUtils.isNotEmpty(periodSchedules)) {
            //3.1 构造每个设备的清洗点清洗计划
            Map<String, List<PeriodVerificationSchedule>> deviceMap = new HashMap<>();
            for (PeriodVerificationSchedule schedule : periodSchedules) {
                deviceMap.computeIfAbsent(schedule.getApparatusId(), k -> new ArrayList<>()).add(schedule);
            }
            //设备信息
            List<String> deviceIds = new ArrayList<>(deviceMap.keySet());
            Map<String, PeriodVerificationDevice> deviceCache = deviceService.listByIds(deviceIds)
                    .stream()
                    .collect(Collectors.toMap(
                            PeriodVerificationDevice::getId,
                            item -> item,
                            (existing, replacement) -> existing
                    ));
            //获取部门信息
            List<SysDepart> deptList = sysDepartService.list();
            //将部门信息按照Code分组
            Map<String, SysDepart> deptMap = deptList.stream()
                    .collect(Collectors.toMap(
                            SysDepart::getOrgCode,
                            item -> item,
                            // 如果 key 重复，保留第一个
                            (existing, replacement) -> existing
                    ));
            PeriodStatisticsDto dto = new PeriodStatisticsDto();
            dto.setDeptCode(periodStatisticsDto.getWorkShopCode());
            int completed = 0;
            int postpone = 0;
            int underWay = 0;

            for (Map.Entry<String, List<PeriodVerificationSchedule>> entry : deviceMap.entrySet()) {
                String deviceId = entry.getKey();
                List<PeriodVerificationSchedule> schedules = entry.getValue();
                PeriodVerificationDevice device = deviceCache.get(deviceId);
                if(null != device) {
                    PeriodStatisticsDto statisticsDto = createPeriodStatisticsDto(
                            device.getApparatusName(), dto, schedules, monthList, deptMap
                    );
                    completed += statisticsDto.getComplete();
                    postpone += statisticsDto.getDelay();
                    underWay += statisticsDto.getPlan();
                }
            }
            CopPlanRank completedDto = new CopPlanRank();
            completedDto.setName("completed");
            completedDto.setSize(completed);
            result.add(completedDto);
            CopPlanRank postponeDto = new CopPlanRank();
            postponeDto.setName("postpone");
            postponeDto.setSize(postpone);
            result.add(postponeDto);
            CopPlanRank underWayDto = new CopPlanRank();
            underWayDto.setName("underWay");
            underWayDto.setSize(underWay);
            result.add(underWayDto);
        }
        return result;
    }

    private void processWeekAndAddToMonthList(int month,List<String> monthList) {
        MdmWeek week = new MdmWeek();
        week.setYear(String.valueOf(DateUtils.getCurrentYear()));
        week.setMonth(String.valueOf(month));
        List<MdmWeek> weekList = mdmWeekService.getWeek(week);
        if(CollectionUtils.isNotEmpty(weekList)) {
            for(MdmWeek mdmWeek : weekList) {
                monthList.add(String.valueOf(mdmWeek.getWeekNumber()));
            }
        }
    }

    /**
     * 处理每周的排期数据
     * @param name
     * @param schedules
     * @param monthList
     * @return
     */
    private CopPlanRank createWeekStatisticsDto(String name, List<PeriodVerificationSchedule> schedules, List<String> monthList) {
        CopPlanRank statisticsDto = new CopPlanRank();
        statisticsDto.setName(name);
        // 初始化其他的计数变量
        int count = 0;
        int complete = 0;
        for (PeriodVerificationSchedule schedule : schedules) {
            List<ScheduleJson> list = JSONArray.parseArray(schedule.getWeekJson(), ScheduleJson.class);
            list.removeIf(scheduleJson -> !monthList.contains(String.valueOf(scheduleJson.getNum())));
            for (ScheduleJson json : list) {
                if(json.getState().equals("1")) {
                    complete += 1;
                }
                count += 1;
            }
        }
        if(count != 0) {
            double value = (double) complete / count;
            BigDecimal rate = new BigDecimal(Double.toString(value));
            rate = rate.setScale(2, RoundingMode.HALF_UP);
            statisticsDto.setLv(rate.doubleValue());
        } else {
            statisticsDto.setLv(0.0);
        }
        statisticsDto.setSize(count);
        return statisticsDto;
    }

    /**
     * 处理每周的排期数据
     * @param name
     * @param schedules
     * @param monthList
     * @return
     */
    private CopPlanRank createLineStatisticsDto(String name, List<PeriodVerificationSchedule> schedules, List<String> monthList) {
        CopPlanRank statisticsDto = new CopPlanRank();
        statisticsDto.setName(name);
        // 初始化其他的计数变量
        int plan = 0;
        int complete = 0;
        int delay = 0;
        for (PeriodVerificationSchedule schedule : schedules) {
            List<ScheduleJson> list = JSONArray.parseArray(schedule.getWeekJson(), ScheduleJson.class);
            list.removeIf(scheduleJson -> !"6".contains(String.valueOf(scheduleJson.getMonth())));
            for (ScheduleJson json : list) {
                switch (json.getState()) {
                    case "-1": {
                        //计划
                        plan += 1;
                        break;
                    }
                    case "1": {
                        complete += 1;
                        break;
                    }
                    case "2": {
                        delay += 1;
                        break;
                    }
                }
            }
        }
        if("completed".equals(name)){
            statisticsDto.setSize(complete);
        } else if("postpone".equals(name)){
            statisticsDto.setSize(delay);
        } else if("underWay".equals(name)) {
            statisticsDto.setSize(plan);
        }
        return statisticsDto;
    }


    private PeriodStatisticsDto processStatistics(PeriodStatisticsDto periodStatisticsDto) {
        periodStatisticsDto.setTotal(periodStatisticsDto.getCheck()+ periodStatisticsDto.getDelay() + periodStatisticsDto.getDelayComplete() + periodStatisticsDto.getPlan() + periodStatisticsDto.getComplete() + periodStatisticsDto.getNoExecute() + periodStatisticsDto.getNoExecutePlan() + periodStatisticsDto.getUnqualified());
        periodStatisticsDto.setCompleteCount(periodStatisticsDto.getTotal() -  periodStatisticsDto.getNoExecutePlan());
        periodStatisticsDto.setCompleteRate(NumberCalcUtil.calculatePercentage(periodStatisticsDto.getComplete() + periodStatisticsDto.getDelayComplete(), periodStatisticsDto.getCompleteCount(), true));
        return periodStatisticsDto;
    }

    /**
     * 处理每月的排期数据
     * @param name
     * @param periodStatisticsDto
     * @param schedules
     * @param monthList
     * @return
     */
    private PeriodStatisticsDto createPeriodStatisticsDto(
            String name,
            PeriodStatisticsDto periodStatisticsDto,
            List<PeriodVerificationSchedule> schedules,
            List<String> monthList,
            Map<String, SysDepart> deptMap
    ) {
        PeriodStatisticsDto statisticsDto = new PeriodStatisticsDto();
        statisticsDto.setName(name);
        getDeptName(periodStatisticsDto, deptMap, statisticsDto);
        statisticsDto.setYear(periodStatisticsDto.getYear());
        // 初始化其他的计数变量
        int plan = 0;
        int complete = 0;
        int delay = 0;
        int delayComplete = 0;
        int check = 0;
        int noExecute = 0;
        int noExecutePlan = 0;
        int unqualified = 0;
        for (PeriodVerificationSchedule schedule : schedules) {
            List<ScheduleJson> list = JSONArray.parseArray(schedule.getWeekJson(), ScheduleJson.class);
            list.removeIf(scheduleJson -> !monthList.contains(String.valueOf(scheduleJson.getMonth())));
            for (ScheduleJson json : list) {
                switch (json.getState()) {
                    case "-1": {
                        //计划
                        plan += 1;
                        break;
                    }
                    case "0": {
                        unqualified += 1;
                        break;
                    }
                    case "1": {
                        complete += 1;
                        break;
                    }
                    case "2": {
                        delay += 1;
                        break;
                    }
                    case "3": {
                        delayComplete += 1;
                        break;
                    }
                    case "4": {
                        check += 1;
                        break;
                    }
                    case "5": {
                        noExecute += 1;
                        break;
                    }
                    case "6": {
                        noExecutePlan += 1;
                        break;
                    }
                }
            }
        }
        statisticsDto.setPlan(plan);
        statisticsDto.setComplete(complete);
        statisticsDto.setDelay(delay);
        statisticsDto.setDelayComplete(delayComplete);
        statisticsDto.setCheck(check);
        statisticsDto.setNoExecute(noExecute);
        statisticsDto.setNoExecutePlan(noExecutePlan);
        statisticsDto.setUnqualified(unqualified);
        processStatistics(statisticsDto);
        return statisticsDto;
    }

    /**
     * 处理每周、每月、每季、每半年的排期数据
     * @param scheduleJsons
     * @param scheduleVo
     */
    private void processWeek(List<ScheduleJson> scheduleJsons,PeriodScheduleVO scheduleVo){
        scheduleJsons.forEach(scheduleJson -> {
            //未来排期，从排期的日期开始往后推
            if ((scheduleJson.getNum() > scheduleVo.getNum())) {
                scheduleJson.setState("-1");
            }
        });
    }

    private void handleSchedule(List<ScheduleJson> scheduleJsons,PeriodScheduleVO scheduleVo,String frequency){
        if(scheduleVo.getResult() != 1){
            return;
        }
        switch (frequency){
            case "1":
                //每周
                processWeek(scheduleJsons,scheduleVo);
                break;
            case "2":
                //每月
                processMonth(scheduleJsons,scheduleVo);
                break;
            case "3":
                //每季
                processQuarter(scheduleJsons,scheduleVo);
                break;
            case "4":
                //每半年
                processHalfYear(scheduleJsons,scheduleVo);
                break;
            default:
                //一年以上
                break;
        }
    }

    /**
     * 处理每周、每月、每季、每半年的排期数据
     * @param scheduleJsons
     * @param scheduleVo
     */
    private void processMonth(List<ScheduleJson> scheduleJsons,PeriodScheduleVO scheduleVo){
        Map<Integer,Integer> monthMap = getMonthMap(scheduleJsons);
        scheduleJsons.forEach(scheduleJson -> {
            //未来排期，从排期的日期开始往后推
            if (scheduleJson.getMonth() > scheduleVo.getMonths()) {
                int currentNum = scheduleVo.getNum();
                int currentMonthNum = monthMap.get(scheduleVo.getMonths());
                currentNum = currentNum - currentMonthNum;
                int num = monthMap.get(scheduleJson.getMonth());
                if(scheduleJson.getNum() == (currentNum + num)) {
                    scheduleJson.setState("-1");
                }
            }
        });
    }

    private Map<Integer,Integer> getMonthMap(List<ScheduleJson> scheduleJsons){
        LinkedHashMap<Integer, List<ScheduleJson>> weekGroup = scheduleJsons.stream().sorted(Comparator.comparing(ScheduleJson::getNum)).collect(Collectors.groupingBy(ScheduleJson::getMonth, LinkedHashMap::new, Collectors.toList()));
        Map<Integer,Integer> monthMap = new HashMap<>();
        for (Map.Entry<Integer, List<ScheduleJson>> stringListEntry : weekGroup.entrySet()) {
            monthMap.put(stringListEntry.getKey(),stringListEntry.getValue().get(0).getNum());
        }
        return monthMap;
    }

    /**
     * 处理每周、每月、每季、每半年的排期数据
     * @param scheduleJsons
     * @param scheduleVo
     */
    private void processQuarter(List<ScheduleJson> scheduleJsons,PeriodScheduleVO scheduleVo){
        LinkedHashMap<Integer, List<ScheduleJson>> quarterGroup = scheduleJsons.stream().sorted(Comparator.comparing(ScheduleJson::getNum)).collect(Collectors.groupingBy(ScheduleJson::getQuarter, LinkedHashMap::new, Collectors.toList()));
        Map<Integer,Integer> quarterMap = new HashMap<>();
        for (Map.Entry<Integer, List<ScheduleJson>> integerListEntry : quarterGroup.entrySet()) {
            quarterMap.put(integerListEntry.getKey(),integerListEntry.getValue().get(0).getNum());
        }
        scheduleJsons.forEach(scheduleJson -> {
            //当前季度的第几周以及以后季度的第几周
            if (scheduleJson.getQuarter() > scheduleVo.getQuarter() && scheduleJson.getNum() > scheduleVo.getNum()) {
                int currentNum = scheduleVo.getNum();
                int currentMonthNum = quarterMap.get(scheduleVo.getQuarter());
                currentNum = currentNum - currentMonthNum;
                int num =currentNum -1 + quarterMap.get(scheduleJson.getQuarter());
                if(scheduleJson.getNum() == num) {
                    scheduleJson.setState("-1");
                }
            }
        });
    }

    /**
     * 处理每周、每月、每季、每半年的排期数据
     * @param scheduleJsons
     * @param scheduleVo
     */
    private void processHalfYear(List<ScheduleJson> scheduleJsons,PeriodScheduleVO scheduleVo){
        if(scheduleVo.getHalfYear() != 1){
            LinkedHashMap<Integer, List<ScheduleJson>> halfYearGroup = scheduleJsons.stream().sorted(Comparator.comparing(ScheduleJson::getNum)).collect(Collectors.groupingBy(ScheduleJson::getHalfYear, LinkedHashMap::new, Collectors.toList()));
            Map<Integer,Integer> halfYearMap = new HashMap<>();
            for (Map.Entry<Integer, List<ScheduleJson>> integerListEntry : halfYearGroup.entrySet()) {
                halfYearMap.put(integerListEntry.getKey(),integerListEntry.getValue().get(0).getNum());
            }
            int currentNum = scheduleVo.getNum();
            int currentMonthNum = halfYearMap.get(scheduleVo.getHalfYear());
            //当前未当月的第几周
            currentNum = currentNum - currentMonthNum;
            for (ScheduleJson scheduleJson : scheduleJsons) {
                //未来排期，从排期的日期开始往后推
                if(scheduleVo.getHalfYear() == 0) {
                    if (scheduleJson.getNum() == (halfYearMap.get(1) + currentNum)) {
                        scheduleJson.setState("-1");
                    }
                }

            }
        }
    }

    private int handleQuarter(int month) {
        if (month == 1 || month == 2 || month == 3) {
            return 1;
        } else if (month == 4 || month == 5 || month == 6) {
            return 2;
        } else if (month == 7 || month == 8 || month == 9) {
            return 3;
        } else {
            return 4;
        }
    }

    /**
     * 构造填充排期数据
     * @param scheduleJson
     * @param scheduleVo
     */
    private  void handleJson(ScheduleJson scheduleJson, PeriodScheduleVO scheduleVo){
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy.MM.dd");
        if(null != scheduleJson && StringUtils.isNotEmpty(scheduleJson.getData())) {
            String[] split = scheduleJson.getData().split("-");

            try {
                // 解析日期字符串
                Date startDate = dataFormat.parse(scheduleJson.getYear() + "." + split[0]);
                Date endDate = dataFormat.parse(scheduleJson.getYear() + "." + split[0]);
                if(startDate.after(endDate)){
                    startDate = dataFormat.parse(scheduleJson.getYear() - 1 + "." + split[0]);
                }
                // 格式化日期为标准格式
                scheduleJson.setDateStart(startDate);
                scheduleJson.setDateEnd(endDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            //判断季度
            scheduleJson.setQuarter(handleQuarter(scheduleJson.getMonth()));
            //判断上半年还是下半年
            if (scheduleJson.getMonth() <= 6) {
                scheduleJson.setHalfYear(0);
            } else {
                scheduleJson.setHalfYear(1);
            }
        }
        if(null != scheduleVo && StringUtils.isNotEmpty(scheduleVo.getDate())){
            String[] voSplit = scheduleVo.getDate().split("-");
            MdmWeek mdmWeek = new MdmWeek();
            try {
                // 解析日期字符串
                Date startDate = dataFormat.parse(scheduleVo.getPlanYear() + "." + voSplit[0]);
                Date endDate = dataFormat.parse(scheduleVo.getPlanYear() + "." + voSplit[1]);
                if(startDate.after(endDate)){
                    startDate = dataFormat.parse(scheduleJson.getYear() - 1 + "." + voSplit[0]);
                }
                mdmWeek.setStartDate(startDate);
                mdmWeek.setEndDate(endDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            mdmWeek.setYear(scheduleVo.getPlanYear());
            List<MdmWeek> weekList = mdmWeekService.getWeek(mdmWeek);
            MdmWeek week;
            if (!weekList.isEmpty()) {
                week = weekList.get(0);
                scheduleVo.setNum(week.getWeekNumber());
                scheduleVo.setMonths(Integer.parseInt(week.getMonth()));
                //判断季度
                scheduleVo.setQuarter(handleQuarter(Integer.parseInt(week.getMonth())));
                //判断上半年还是下半年
                if (Integer.parseInt(week.getMonth()) <= 6) {
                    scheduleVo.setHalfYear(0);
                } else {
                    scheduleVo.setHalfYear(1);
                }
            } else {
                throw new JeecgBootException("未找到对应数据");
            }
        }
    }

    /**
     * 获取指定年份的指定月份的周数据
     * @param months
     * @param weeks
     * @param schedule
     */
    private void populateWeeks(List<String> months, List<MdmWeek> weeks, PeriodVerificationSchedule schedule) {
        months.forEach(month->{
            List<MdmWeek> weekList = mdmWeekService.getWeek(new MdmWeek().setYear(String.valueOf(schedule.getPlanYear())).setMonth(month));
            weeks.addAll(weekList);
        });
    }

    /**
     * 构造空的排期数据
     * @param week
     * @return
     */
    private ScheduleJson createScheduleJson(MdmWeek week) {
        ScheduleJson scheduleJson = new ScheduleJson();
        scheduleJson.setMonth(Integer.valueOf(week.getMonth()));
        scheduleJson.setYear(Integer.valueOf(week.getYear()));
        scheduleJson.setNum(week.getWeekNumber());
        scheduleJson.setData(SIMPLE_DATE_FORMAT.format(week.getStartDate()) + "-" + SIMPLE_DATE_FORMAT.format(week.getEndDate()));
        scheduleJson.setState("");
        return scheduleJson;
    }
}
