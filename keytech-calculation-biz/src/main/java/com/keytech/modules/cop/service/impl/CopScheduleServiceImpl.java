package com.keytech.modules.cop.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.common.NumberCalcUtil;
import com.keytech.modules.cop.dto.*;
import com.keytech.modules.cop.entity.CopDevice;
import com.keytech.modules.cop.entity.CopPoint;
import com.keytech.modules.cop.entity.CopSchedule;
import com.keytech.modules.cop.entity.CopSubmit;
import com.keytech.modules.cop.mapper.CopScheduleMapper;
import com.keytech.modules.cop.service.ICopDeviceService;
import com.keytech.modules.cop.service.ICopPointService;
import com.keytech.modules.cop.service.ICopScheduleService;
import com.keytech.modules.cop.service.ICopSubmitService;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.impl.MdmWeekServiceImpl;
import lombok.extern.slf4j.Slf4j;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * cop计划排期 服务实现类
 * </p>
 *
 * @author lijun
 * @since 2025-02-20 02:19:51
 */
@Slf4j
@Service
public class CopScheduleServiceImpl extends BaseServiceImpl<CopScheduleMapper, CopSchedule> implements ICopScheduleService {
    @Autowired
    private ICopPointService iCopPointService;
    @Autowired
    private MdmWeekServiceImpl mdmWeekService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private ICopDeviceService deviceService;
    @Autowired
    private ICopSubmitService iCopSubmitService;
    @Autowired
    private ISysDepartService sysDepartService;

    @Override
    public List<CopScheduleJson> initScheduleJsonList(CopSchedule copSchedule) throws JeecgBootException{
        List<CopScheduleJson> copScheduleJsons;
        try {
            copScheduleJsons = JSONArray.parseArray(copSchedule.getWeekJson(), CopScheduleJson.class);
        }catch (Exception e){
            log.error("weekJson转换异常--copSchedule:{}",copSchedule.toString(),e);
            throw new JeecgBootException("weekJson转换异常");
        }
        return copScheduleJsons;
    }

    private int determineQuarter(int month) {
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

    @Override
    public CopScheduleJson processJson(CopScheduleJson copScheduleJson) throws Exception{
        SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM.dd");
        if (StringUtils.isNotBlank(copScheduleJson.getData())) {
            //拆分时间字符串组装到单独字段
            String[] split = copScheduleJson.getData().split("-");
            copScheduleJson.setDateStart(SIMPLE_DATE_FORMAT.parse(split[0]));
            copScheduleJson.setDateEnd(SIMPLE_DATE_FORMAT.parse(split[1]));
            //判断季度
            copScheduleJson.setQuarter(determineQuarter(copScheduleJson.getMonth()));
            //判断上半年还是下半年
            if (copScheduleJson.getMonth()<=6) {
                copScheduleJson.setHalfYear(0);
            }else {
                copScheduleJson.setHalfYear(1);
            }
        }
        return copScheduleJson;
    }

    private CopScheduleJson createCopScheduleJson(MdmWeek week) {
        SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM.dd");
        CopScheduleJson copScheduleJson = new CopScheduleJson();
        copScheduleJson.setMonth(Integer.valueOf(week.getMonth()));
        copScheduleJson.setYear(Integer.valueOf(week.getYear()));
        copScheduleJson.setNum(week.getWeekNumber());
        copScheduleJson.setData(SIMPLE_DATE_FORMAT.format(week.getStartDate()) + "-" + SIMPLE_DATE_FORMAT.format(week.getEndDate()));
        copScheduleJson.setState("");
        return copScheduleJson;
    }


    @Override
    public Page<CopSchedule> initCopScheduleList(CopSchedule copScheduleRequestVO, List<String> months, Integer pageNo, Integer pageSize) {
        Page<CopSchedule> copScheduleList = new Page<>(pageNo, pageSize);
        List<CopSchedule> copScheduleDtoList = new ArrayList<>();
//        CopSchedule copScheduleDto = new CopSchedule();
        //1.根据查询条件获取所有清洗点列表
        //查出配置项组装数据
        Page<CopPoint> copPointPage = new Page<>(pageNo, pageSize);
        //防止数据过大，分页清洗点
        if(copScheduleRequestVO.getFactoryCode().equals(copScheduleRequestVO.getDeptCode())){
            copScheduleRequestVO.setDeptCode(null);
        }
        copScheduleRequestVO.setFactoryCode(null);
        Page<CopPoint> page = iCopPointService.page(copPointPage, new LambdaQueryWrapper<CopPoint>()
                .eq(StringUtils.isNotBlank(copScheduleRequestVO.getDeptCode()), CopPoint::getDeptCode, copScheduleRequestVO.getDeptCode())
                .eq(StringUtils.isNotBlank(copScheduleRequestVO.getLineCode()), CopPoint::getLineCode, copScheduleRequestVO.getLineCode())
                .like(StringUtils.isNotBlank(copScheduleRequestVO.getCopDeviceName()), CopPoint::getCopDeviceName, "%" + copScheduleRequestVO.getCopDeviceName() + "%")
                .like(StringUtils.isNotBlank(copScheduleRequestVO.getCopPointName()), CopPoint::getPointName, "%" + copScheduleRequestVO.getCopPointName() + "%")
                .eq(StringUtils.isNotBlank(copScheduleRequestVO.getFrequency()), CopPoint::getFrequency, copScheduleRequestVO.getFrequency())
                .eq(StringUtils.isNotBlank(copScheduleRequestVO.getClearBy()), CopPoint::getClearBy, copScheduleRequestVO.getClearBy())
                .eq(StringUtils.isNotBlank(copScheduleRequestVO.getCopDeviceId()), CopPoint::getCopDeviceId, copScheduleRequestVO.getCopDeviceId())
                .eq(StringUtils.isNotBlank(copScheduleRequestVO.getCopPointId()), CopPoint::getId, copScheduleRequestVO.getCopPointId())
                .eq(CopPoint::getActive, 1)
                        .orderByDesc(CopPoint::getCopDeviceId)
                .orderByAsc(CopPoint::getSort)
        );
        List<CopPoint> copPoints = page.getRecords();
        if (CollectionUtils.isNotEmpty(copPoints)) {
            copScheduleList.setTotal(page.getTotal());
            copScheduleList.setCurrent(page.getCurrent());
            //2.在清洗点列表基础上，适配各个清洗点对应的已有计划信息
            //3.如果没有排期，默认适配当年
            List<CopScheduleJson> weeksJson = new ArrayList<>();
            List<MdmWeek> weeks = new ArrayList<>();
            populateWeeks(months, weeks, copScheduleRequestVO);
            weeks.forEach(week -> weeksJson.add(createCopScheduleJson(week)));

            //在点位信息上追加构造的排期
            //4.最终返回清洗点列表和个清洗点对应的每月计划排期
            for (CopPoint copPoint : copPoints) {
                CopScheduleDto scheduleDto = new CopScheduleDto();
                BeanUtils.copyProperties(copPoint, scheduleDto);
                scheduleDto.setCopPointId(copPoint.getId());
                scheduleDto.setCopPointName(copPoint.getPointName());
                scheduleDto.setRowKey(copPoint.getId());
                CopSchedule schedule = this.getOne(new LambdaQueryWrapper<CopSchedule>().eq(CopSchedule::getCopPointId, copPoint.getId()).eq(CopSchedule::getPlanYear, copScheduleRequestVO.getPlanYear()));
                if (null != schedule) {
                    scheduleDto.setId(schedule.getId());
                    if(StringUtils.isNotEmpty(schedule.getFrequency())){
                        scheduleDto.setFrequency(schedule.getFrequency());
                    }

                    String weekJson = schedule.getWeekJson();
                    if (StringUtils.isNotBlank(weekJson)) {
                        List<CopScheduleJson> copScheduleJsons = JSONArray.parseArray(weekJson, CopScheduleJson.class);
                        copScheduleJsons = copScheduleJsons.stream()
                                .sorted(Comparator.comparing(CopScheduleJson::getNum))
                                .toList();
                        //排除当前选择的月份其他的数据
                        if (months.size() < 12) {
                            copScheduleJsons = new ArrayList<>(copScheduleJsons);
                            copScheduleJsons.removeIf(copScheduleJson -> !months.contains(String.valueOf(copScheduleJson.getMonth())));
                        }
                        scheduleDto.setPlanYear(schedule.getPlanYear());
                        scheduleDto.setWeekJson(JSONArray.toJSONString(copScheduleJsons));
                    } else {
                        scheduleDto.setWeekJson(JSONArray.toJSONString(weeksJson));
                    }
                    scheduleDto.setPlanYear(copScheduleRequestVO.getPlanYear());

                    // 确认 weeksJson 是传入的参数
                    copScheduleDtoList.add(scheduleDto);
                } else {
                    scheduleDto.setWeekJson(JSONArray.toJSONString(weeksJson));
                    scheduleDto.setPlanYear(copScheduleRequestVO.getPlanYear());
                    // 确认 weeksJson 是传入的参数
                    copScheduleDtoList.add(scheduleDto);
                }
            }
        }
        return copScheduleList.setRecords(copScheduleDtoList);
    }



    @Override
    public CopSchedule saveOrUpdateCopScheduler(CopSchedulerVo copScheduleVo) {
        SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM.dd");
        //1.获取到用户的排期计划，根据当前年和清洗点id查询排期计划
        CopSchedule oldCopSchedule = this.getOne(new LambdaQueryWrapper<CopSchedule>()
                        .eq(StringUtils.isNotBlank(copScheduleVo.getId()), CopSchedule::getId, copScheduleVo.getId())
                .eq(CopSchedule::getCopPointId, copScheduleVo.getCopPointId())
                .eq(CopSchedule::getPlanYear, copScheduleVo.getPlanYear()));
        String frequency = copScheduleVo.getFrequency();
        List<CopScheduleJson> scheduleJsons;
        if(null != oldCopSchedule){
            if (StringUtils.isNotBlank(oldCopSchedule.getWeekJson())) {
                // 2.如果存在，则更新排期数据
                scheduleJsons = JSONArray.parseArray(oldCopSchedule.getWeekJson(), CopScheduleJson.class);
                // 2.1 需要更新当前提交的周数的排期
                boolean flag = false;
                for (CopScheduleJson json : scheduleJsons) {
                    handleJson(json, copScheduleVo);
                    if (json.getNum() >= copScheduleVo.getNum() && StringUtils.isNotEmpty(json.getState())) {
                        flag = true;
                    }
                }
                if (flag) {
                    scheduleJsons.forEach(scheduleJson -> {
                        handleJson(scheduleJson, copScheduleVo);
                        if (scheduleJson.getNum() == copScheduleVo.getNum()) {
                            if (copScheduleVo.getResult() == 1) {
                                scheduleJson.setState("-1");
                            } else if(copScheduleVo.getResult() == 2){
                                scheduleJson.setState("6");
                            } else {
                                scheduleJson.setState("");
                            }
                        }
                    });
                } else {
                    // 2.2首先清空当前提交的周数之后的排期数据
                    scheduleJsons.forEach(scheduleJson -> {
                        handleJson(scheduleJson, copScheduleVo);
                        if (scheduleJson.getNum() == copScheduleVo.getNum()) {
                            if (copScheduleVo.getResult() == 1) {
                                scheduleJson.setState("-1");
                            } else if(copScheduleVo.getResult() == 2){
                                scheduleJson.setState("6");
                            } else {
                                scheduleJson.setState("");
                            }
                        }
                        // //上个版本的逻辑，如果存在排期，重新排期的话，需要先清空当前周后原有排期
                        //现有逻辑，如果已有排期则只排当前周的，其他不变
                        else if (scheduleJson.getNum() > copScheduleVo.getNum()) {
                            scheduleJson.setState("");
                        }
                    });
                    // 2.3 然后更新当前提交的周数的排期数据
                    handleSchedule(scheduleJsons, copScheduleVo, frequency);
                }
                oldCopSchedule.setWeekJson(JSONArray.toJSONString(scheduleJsons));
            } else {
                // 3.1 首先构造空的排期数据
                //如果没有数据，用周接口获取一个模板
                MdmWeek mdmWeek = new MdmWeek();
                mdmWeek.setYear(String.valueOf(copScheduleVo.getPlanYear()));
                List<MdmWeek> weekList = mdmWeekService.getWeek(mdmWeek);
                scheduleJsons = new ArrayList<>();
                for (MdmWeek week : weekList) {
                    CopScheduleJson newScheduleJson = new CopScheduleJson();
                    newScheduleJson.setYear(Integer.valueOf(week.getYear()));
                    newScheduleJson.setMonth(Integer.valueOf(week.getMonth()));
                    newScheduleJson.setNum(week.getWeekNumber());
                    newScheduleJson.setData(SIMPLE_DATE_FORMAT.format(week.getStartDate()) + "-" + SIMPLE_DATE_FORMAT.format(week.getEndDate()));
                    newScheduleJson.setState("");
                    handleJson(newScheduleJson, copScheduleVo);
                    scheduleJsons.forEach(scheduleJson -> {
                        handleJson(scheduleJson, copScheduleVo);
                        if (newScheduleJson.getNum() == copScheduleVo.getNum()) {
                            if (copScheduleVo.getResult() == 1) {
                                newScheduleJson.setState("-1");
                            } else {
                                newScheduleJson.setState("6");
                            }
                        } else if (newScheduleJson.getNum() > copScheduleVo.getNum()) {
                            newScheduleJson.setState("");
                        }
                    });
                    //3.6 如果是一年或者以上的，只排当前提交周的
                    scheduleJsons.add(newScheduleJson);
                    // 2.3 然后更新当前提交的周数的排期数据
                }
                handleSchedule(scheduleJsons, copScheduleVo, frequency);
                oldCopSchedule.setWeekJson(JSONArray.toJSONString(scheduleJsons));
            }
            this.updateById(oldCopSchedule);
        } else {
            // 3.如果不存在，则新增排期数据
            oldCopSchedule = new CopSchedule();
            oldCopSchedule.setCopPointId(copScheduleVo.getCopPointId());
            CopPoint copPoint = iCopPointService.getOne(new LambdaQueryWrapper<CopPoint>().eq(CopPoint::getId, copScheduleVo.getCopPointId()));
            BeanUtils.copyProperties(copPoint, oldCopSchedule, "id");
            oldCopSchedule.setPlanYear(Integer.valueOf(copScheduleVo.getPlanYear()));
            oldCopSchedule.setFrequency(frequency);
            oldCopSchedule.setCopPointName(copPoint.getPointName());
            oldCopSchedule.setCopDeviceName(copPoint.getCopDeviceName());
            oldCopSchedule.setClearBy(copScheduleVo.getClearBy());
            oldCopSchedule.setCheckBy(copScheduleVo.getCheckBy());
            // 3.1 首先构造空的排期数据
            //如果没有数据，用周接口获取一个模板
            MdmWeek mdmWeek = new MdmWeek();
            mdmWeek.setYear(String.valueOf(copScheduleVo.getPlanYear()));
            List<MdmWeek> weekList = mdmWeekService.getWeek(mdmWeek);
            scheduleJsons = new ArrayList<>();
            for (MdmWeek week : weekList) {
                CopScheduleJson newScheduleJson = new CopScheduleJson();
                newScheduleJson.setYear(Integer.valueOf(week.getYear()));
                newScheduleJson.setMonth(Integer.valueOf(week.getMonth()));
                newScheduleJson.setNum(week.getWeekNumber());
                newScheduleJson.setData(SIMPLE_DATE_FORMAT.format(week.getStartDate()) + "-" + SIMPLE_DATE_FORMAT.format(week.getEndDate()));
                newScheduleJson.setState("");
                handleJson(newScheduleJson, copScheduleVo);
                //3.6 如果是一年或者以上的，只排当前提交周的
                scheduleJsons.add(newScheduleJson);
            }
            for (CopScheduleJson scheduleJson : scheduleJsons) {
                    //先处理当前提交的周
                    if (scheduleJson.getNum() == copScheduleVo.getNum()) {
                        if (copScheduleVo.getResult() == 1) {
                            scheduleJson.setState("-1");
                        } else {
                            scheduleJson.setState("6");
                        }
                    } else {
                        scheduleJson.setState("");
                    }
                    handleSchedule(scheduleJsons, copScheduleVo, frequency);
            }
            oldCopSchedule.setWeekJson(JSONArray.toJSONString(scheduleJsons));
            this.save(oldCopSchedule);
        }
        return oldCopSchedule;
    }

    @Override
    public CopSchedule cancelCopScheduler(CopSchedulerVo copScheduleVo) {
        //1.获取到用户的排期计划，根据当前年和清洗点id查询排期计划
        CopSchedule oldCopSchedule = this.getOne(new LambdaQueryWrapper<CopSchedule>()
                .eq(StringUtils.isNotBlank(copScheduleVo.getId()), CopSchedule::getId, copScheduleVo.getId())
                .eq(CopSchedule::getCopPointId, copScheduleVo.getCopPointId())
                .eq(CopSchedule::getPlanYear, copScheduleVo.getPlanYear()));
        List<CopScheduleJson> scheduleJsons;
        if(null != oldCopSchedule) {
            if (StringUtils.isNotBlank(oldCopSchedule.getWeekJson())) {
                // 2.如果存在，则更新排期数据
                scheduleJsons = JSONArray.parseArray(oldCopSchedule.getWeekJson(), CopScheduleJson.class);
// 2.1 需要更新当前提交的周数的排期
                // 2.2首先清空当前提交的周数之后的排期数据
                scheduleJsons.forEach(scheduleJson -> {
                    handleJson(scheduleJson, copScheduleVo);
                    if(copScheduleVo.getNum() == 0){
                        throw new JeecgBootException("对应周错误");
                    }
                    if(copScheduleVo.getType() == 0){
                        if (scheduleJson.getNum() == copScheduleVo.getNum()) {
                            scheduleJson.setState("");
                        }
                    } else {
                        if (scheduleJson.getNum() >= copScheduleVo.getNum()) {
                            scheduleJson.setState("");
                        }
                    }
                });
                // 2.3 然后更新当前提交的周数的排期数据
                //handleSchedule(scheduleJsons, copScheduleVo, frequency);
                oldCopSchedule.setWeekJson(JSONArray.toJSONString(scheduleJsons));
            }
            this.updateById(oldCopSchedule);
        }
        return oldCopSchedule;
    }

    @Override
    public void updateCop(List<CopSchedulerVo> list) throws Exception {
        for (CopSchedulerVo copScheduleVo : list) {
            CopSchedule copSchedule = this.getById(copScheduleVo.getId());
            CopPoint copPoint = null;
            if(null != copSchedule) {
                copPoint = iCopPointService.getById(copSchedule.getCopPointId());
                if(StringUtils.isNotEmpty(copScheduleVo.getFrequency())) {
                    copSchedule.setFrequency(copScheduleVo.getFrequency());
                    copPoint.setFrequency(copScheduleVo.getFrequency());
                }
                if(StringUtils.isNotEmpty(copScheduleVo.getFormula())) {
                    copSchedule.setFormula(copScheduleVo.getFormula());
                    copPoint.setFormula(copScheduleVo.getFormula());
                }
                if(StringUtils.isNotEmpty(copScheduleVo.getTemperature())) {
                    copSchedule.setTemperature(copScheduleVo.getTemperature());
                    copPoint.setTemperature(copScheduleVo.getTemperature());
                }
                if(StringUtils.isNotEmpty(copScheduleVo.getClearBy())) {
                    copSchedule.setClearBy(copScheduleVo.getClearBy());
                    copPoint.setClearBy(copScheduleVo.getClearBy());
                }
                if(StringUtils.isNotEmpty(copScheduleVo.getCheckBy())) {
                    copSchedule.setCheckBy(copScheduleVo.getCheckBy());
                    copPoint.setCheckBy(copScheduleVo.getCheckBy());
                }
                if(StringUtils.isNotEmpty(copScheduleVo.getSort())){
                    copSchedule.setSort(Double.parseDouble(copScheduleVo.getSort()));
                    copPoint.setSort(Double.parseDouble(copScheduleVo.getSort()));
                } else {
                    copSchedule.setSort(null);
                    copPoint.setSort(null);
                }
                this.updateById(copSchedule);
            } else {
                copPoint = iCopPointService.getById(copScheduleVo.getId());
                if(StringUtils.isNotEmpty(copScheduleVo.getFrequency())) {
                    copPoint.setFrequency(copScheduleVo.getFrequency());
                }
                if(StringUtils.isNotEmpty(copScheduleVo.getFormula())) {
                    copPoint.setFormula(copScheduleVo.getFormula());
                }
                if(StringUtils.isNotEmpty(copScheduleVo.getTemperature())) {
                    copPoint.setTemperature(copScheduleVo.getTemperature());
                }
                if(StringUtils.isNotEmpty(copScheduleVo.getClearBy())) {
                    copPoint.setClearBy(copScheduleVo.getClearBy());
                }
                if(StringUtils.isNotEmpty(copScheduleVo.getCheckBy())) {
                    copPoint.setCheckBy(copScheduleVo.getCheckBy());
                }
                if(StringUtils.isNotEmpty(copScheduleVo.getSort())){
                    copPoint.setSort(Double.parseDouble(copScheduleVo.getSort()));
                } else {
                    copPoint.setSort(null);
                }
            }
            iCopPointService.updateById(copPoint);
        }
    }

    @Override
    public CopSchedule processCopSchedule(String id, int num, String status) {
        CopSchedule schedule = this.getById(id);
        if(null != schedule){
            List<CopScheduleJson> weekList = JSONArray.parseArray(schedule.getWeekJson(), CopScheduleJson.class);
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
        if("-1".equals(status)) {
//            iCopSubmitService.remove(new LambdaQueryWrapper<CopSubmit>().eq(CopSubmit::getCopScheduleId, id));
            iCopSubmitService.remove(
                    new LambdaQueryWrapper<CopSubmit>()
                            .eq(CopSubmit::getCopScheduleId, id)
                            .eq(CopSubmit::getPlanWeek, num)
            );
        }
        return schedule;
    }

    private void handleSchedule(List<CopScheduleJson> scheduleJsons, CopSchedulerVo scheduleVo, String frequency){
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
    private void processWeek(List<CopScheduleJson> scheduleJsons,CopSchedulerVo scheduleVo){
        scheduleJsons.forEach(scheduleJson -> {
            //未来排期，从排期的日期开始往后推
            if ((scheduleJson.getNum() > scheduleVo.getNum())) {
                scheduleJson.setState("-1");
            }
        });
    }

    /**
     * 处理每周、每月、每季、每半年的排期数据
     * @param scheduleJsons
     * @param scheduleVo
     */
    private void processMonth(List<CopScheduleJson> scheduleJsons,CopSchedulerVo scheduleVo){
        Map<Integer,Integer> monthMap = getMonthMap(scheduleJsons);
        scheduleJsons.forEach(scheduleJson -> {
            //未来排期，从排期的日期开始往后推
            if (scheduleJson.getMonth() > scheduleVo.getMonth()) {
                //当前周数
                int currentNum = scheduleVo.getNum();
                //当前月第一周周数
                int currentMonthNum = monthMap.get(scheduleVo.getMonth());
                //计算相差周数
                currentNum = currentNum - currentMonthNum;
                //获取当前排期的周数
                int num = monthMap.get(scheduleJson.getMonth());
                //如果后续周数等于当前排期周数加上相差周数
                if(scheduleJson.getNum() == (currentNum + num)) {
                    scheduleJson.setState("-1");
                }
            }
        });
    }

    private Map<Integer,Integer> getMonthMap(List<CopScheduleJson> scheduleJsons){
        LinkedHashMap<Integer, List<CopScheduleJson>> weekGroup = scheduleJsons.stream().sorted(Comparator.comparing(CopScheduleJson::getNum)).collect(Collectors.groupingBy(CopScheduleJson::getMonth, LinkedHashMap::new, Collectors.toList()));
        Map<Integer,Integer> monthMap = new HashMap<>();
        for (Map.Entry<Integer, List<CopScheduleJson>> stringListEntry : weekGroup.entrySet()) {
            monthMap.put(stringListEntry.getKey(),stringListEntry.getValue().get(0).getNum());
        }
        return monthMap;
    }

    /**
     * 处理每周、每月、每季、每半年的排期数据
     * @param scheduleJsons
     * @param scheduleVo
     */
    private void processQuarter(List<CopScheduleJson> scheduleJsons,CopSchedulerVo scheduleVo){
        LinkedHashMap<Integer, List<CopScheduleJson>> quarterGroup = scheduleJsons.stream().sorted(Comparator.comparing(CopScheduleJson::getNum)).collect(Collectors.groupingBy(CopScheduleJson::getQuarter, LinkedHashMap::new, Collectors.toList()));
        Map<Integer,Integer> quarterMap = new HashMap<>();
        for (Map.Entry<Integer, List<CopScheduleJson>> integerListEntry : quarterGroup.entrySet()) {
            quarterMap.put(integerListEntry.getKey(),integerListEntry.getValue().get(0).getNum());
        }
        scheduleJsons.forEach(scheduleJson -> {

            //当前季度的第几周以及以后季度的第几周
            if (scheduleJson.getQuarter() > scheduleVo.getQuarter() && scheduleJson.getNum() > scheduleVo.getNum()) {
                int currentNum = scheduleVo.getNum();
                int currentMonthNum = quarterMap.get(scheduleVo.getQuarter());
                currentNum = currentNum - currentMonthNum;
                int num = currentNum -1 + quarterMap.get(scheduleJson.getQuarter());
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
    private void processHalfYear(List<CopScheduleJson> scheduleJsons,CopSchedulerVo scheduleVo){
        if(scheduleVo.getHalfYear() != 1){
            LinkedHashMap<Integer, List<CopScheduleJson>> halfYearGroup = scheduleJsons.stream().sorted(Comparator.comparing(CopScheduleJson::getNum)).collect(Collectors.groupingBy(CopScheduleJson::getHalfYear, LinkedHashMap::new, Collectors.toList()));
            Map<Integer,Integer> halfYearMap = new HashMap<>();
            for (Map.Entry<Integer, List<CopScheduleJson>> integerListEntry : halfYearGroup.entrySet()) {
                halfYearMap.put(integerListEntry.getKey(),integerListEntry.getValue().get(0).getNum());
            }
            int currentNum = scheduleVo.getNum();
            int currentMonthNum = halfYearMap.get(scheduleVo.getHalfYear());
            //当前未当月的第几周
            currentNum = currentNum - currentMonthNum;
            for (CopScheduleJson scheduleJson : scheduleJsons) {
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
    private  void handleJson(CopScheduleJson scheduleJson, CopSchedulerVo scheduleVo){
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy.MM.dd");
        if(null != scheduleJson && StringUtils.isNotEmpty(scheduleJson.getData())) {
            String[] split = scheduleJson.getData().split("-");

            try {
                // 解析日期字符串
                Date startDate = dataFormat.parse(scheduleJson.getYear() + "." + split[0]);
                Date endDate = dataFormat.parse(scheduleJson.getYear() + "." + split[1]);
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
                    if (scheduleJson != null) {
                        startDate = dataFormat.parse(scheduleJson.getYear() - 1 + "." + voSplit[0]);
                    }
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
                scheduleVo.setMonth(Integer.parseInt(week.getMonth()));
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
    private void populateWeeks(List<String> months, List<MdmWeek> weeks, CopSchedule schedule) {
        List<MdmWeek> weekList = mdmWeekService.getWeek(new MdmWeek().setYear(String.valueOf(schedule.getPlanYear())));
        Map<String, List<MdmWeek>> groupedByMonth = weekList.stream()
                .collect(Collectors.groupingBy(MdmWeek::getMonth));
        months.forEach(month->{
            if(null != groupedByMonth.get(month)){
                weeks.addAll(groupedByMonth.get(month));
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CopStatisticsDto>  copStatistics(CopStatisticsDto copStatisticsDto) throws Exception{
        List<CopStatisticsDto> result = new ArrayList<>();
        //1.获取当前所有排期数据
        LambdaQueryWrapper<CopSchedule> queryWrapper = new LambdaQueryWrapper<CopSchedule>()
                .select(CopSchedule::getId, CopSchedule::getPlanYear, CopSchedule::getCopPointName, CopSchedule::getCopDeviceId, CopSchedule::getCopPointId, CopSchedule::getWeekJson, CopSchedule::getCheckBy, CopSchedule::getClearBy)
                .eq(StringUtils.isNotBlank(copStatisticsDto.getDeptCode()), CopSchedule::getDeptCode, copStatisticsDto.getDeptCode())
                .eq(StringUtils.isNotBlank(copStatisticsDto.getLineCode()), CopSchedule::getLineCode, copStatisticsDto.getLineCode())
                .eq(copStatisticsDto.getYear() != 0,CopSchedule::getPlanYear, copStatisticsDto.getYear());
        if(StringUtils.isNotEmpty(copStatisticsDto.getFrequencys())){
            String[] frequencyAtr = copStatisticsDto.getFrequencys().split(",");
            queryWrapper.in(CopSchedule::getFrequency, frequencyAtr);
        }
        List<CopSchedule> copSchedules = this.list(queryWrapper);
        List<String> monthList;

        //2.筛选每个设备下所有清洗点排期周期内容的清洗计划
        if(StringUtils.isNotBlank(copStatisticsDto.getMonths())) {
            monthList = new ArrayList<>();
            String[] str = copStatisticsDto.getMonths().split(",");
            for(String s : str) {
                monthList.add(s);
            }
        } else {
            monthList = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12");
        }
        if (CollectionUtils.isNotEmpty(copSchedules)) {
            if(copStatisticsDto.getType() == 0) {
                //3.1 构造每个设备的清洗点清洗计划
                Map<String, List<CopSchedule>> deviceMap = new HashMap<>();
                for (CopSchedule schedule : copSchedules) {
                    deviceMap.computeIfAbsent(schedule.getCopDeviceId(), k -> new ArrayList<>()).add(schedule);
                }
                deviceMap.forEach((name, schedules) -> {
                    CopDevice copDevice = deviceService.getById(name);
                    if(null != copDevice && copDevice.getActive() == 1) {
                        CopStatisticsDto statisticsDto = createStatisticsDto(copDevice.getCopDeviceName(), copStatisticsDto, schedules, monthList);
                        result.add(statisticsDto);
                    }
                });
            }
            if(copStatisticsDto.getType() == 1) {
                //3.2 按照清洗人分组
                Map<String, List<CopSchedule>> clearMap = new HashMap<>();
                for (CopSchedule schedule : copSchedules) {
                    clearMap.computeIfAbsent(schedule.getClearBy(), k -> new ArrayList<>()).add(schedule);
                }
                clearMap.forEach((clearUser, schedules) -> {
                    SysUser user = userService.getById(clearUser);
                    if(null != user) {
                        CopStatisticsDto statisticsDto = createStatisticsDto(user.getRealname(), copStatisticsDto, schedules, monthList);
                        result.add(statisticsDto);
                    }
                });
            }
            if(copStatisticsDto.getType() == 2) {
                //3.3 按照检查人分组
                Map<String, List<CopSchedule>> checkMap = new HashMap<>();
                for (CopSchedule schedule : copSchedules) {
                    checkMap.computeIfAbsent(schedule.getCheckBy(), k -> new ArrayList<>()).add(schedule);
                }
                checkMap.forEach((checkUser, schedules) -> {
                    SysUser user = userService.getById(checkUser);
                    if(null != user) {
                        CopStatisticsDto statisticsDto = createStatisticsDto(user.getRealname(), copStatisticsDto, schedules, monthList);
                        result.add(statisticsDto);
                    }
                });
            }
        }
        return result;
    }

    @Override
    public List<CopPlanRank> copPlanRank(WSActionStatisticsDto copStatisticsDto) throws Exception {
        List<CopPlanRank> result = new ArrayList<>();
        //1.获取当前所有排期数据
        LambdaQueryWrapper<CopSchedule> queryWrapper = new LambdaQueryWrapper<CopSchedule>()
                .select(CopSchedule::getId, CopSchedule::getPlanYear,
                        CopSchedule::getDeptCode,CopSchedule::getCopPointName, CopSchedule::getCopDeviceId, CopSchedule::getCopPointId, CopSchedule::getWeekJson, CopSchedule::getCheckBy, CopSchedule::getClearBy)
                .eq(CopSchedule::getPlanYear, DateUtils.getCurrentYear());

        List<CopSchedule> copSchedules = this.list(queryWrapper);
        List<String> monthList = new ArrayList<>();;

        //2.筛选每个设备下所有清洗点排期周期内容的清洗计划
        if(copStatisticsDto.getDateType() == 1){
            monthList.add(String.valueOf(DateUtils.getCurrentOfWeek() - 1));
        } else if(copStatisticsDto.getDateType() == 2){
            int month = DateUtils.getCurrentMonth() - 1 ;
            processWeekAndAddToMonthList(month, monthList);
        } else if(copStatisticsDto.getDateType() == 3){
            int month = DateUtils.getCurrentMonth();
            processWeekAndAddToMonthList(month, monthList);
        }
        
        //3.1 构造每个设备的清洗点清洗计划
        Map<String, List<CopSchedule>> deviceMap = new HashMap<>();
        for (CopSchedule schedule : copSchedules) {
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
            List<CopSchedule> departmentSchedules = deviceMap.getOrDefault(department.getOrgCode(), new ArrayList<>());
            CopPlanRank statisticsDto = createWeekStatisticsDto(department.getDepartName(), departmentSchedules, monthList);
            result.add(statisticsDto);
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

    @Override
    public List<CopPlanRank> copLineStatistics(WSActionStatisticsDto copStatisticsDto) throws Exception {
        List<CopPlanRank> result = new ArrayList<>();
        //1.获取当前所有排期数据
        LambdaQueryWrapper<CopSchedule> queryWrapper = new LambdaQueryWrapper<CopSchedule>()
                .select(CopSchedule::getId, CopSchedule::getPlanYear, CopSchedule::getCopPointName, CopSchedule::getCopDeviceId, CopSchedule::getCopPointId, CopSchedule::getWeekJson, CopSchedule::getCheckBy, CopSchedule::getClearBy)
                .eq(CopSchedule::getPlanYear, DateUtils.getCurrentYear()).eq(CopSchedule::getDeptCode, copStatisticsDto.getWorkShopCode());
        List<CopSchedule> copSchedules = this.list(queryWrapper);
        List<String> monthList = new ArrayList<>();;

        //2.筛选每个设备下所有清洗点排期周期内容的清洗计划
        if(copStatisticsDto.getDateType() == 1){
            monthList.add(String.valueOf(DateUtils.getCurrentOfWeek() - 1));
        } else if(copStatisticsDto.getDateType() == 2){
            int month = DateUtils.getCurrentMonth() - 1 ;
            processWeekAndAddToMonthList(month, monthList);
        } else if(copStatisticsDto.getDateType() == 3){
            int month = DateUtils.getCurrentMonth();
            processWeekAndAddToMonthList(month, monthList);
        }
        if (CollectionUtils.isNotEmpty(copSchedules)) {
            //3.1 构造每个设备的清洗点清洗计划
            Map<String, List<CopSchedule>> deviceMap = new HashMap<>();
            for (CopSchedule schedule : copSchedules) {
                deviceMap.computeIfAbsent(schedule.getDeptCode(), k -> new ArrayList<>()).add(schedule);
            }
            deviceMap.forEach((name, schedules) -> {
                CopPlanRank completed =  createLineStatisticsDto("completed", schedules, monthList);
                result.add(completed);
                CopPlanRank postpone =  createLineStatisticsDto("postpone", schedules, monthList);
                result.add(postpone);
                CopPlanRank underWay =  createLineStatisticsDto("underWay", schedules, monthList);
                result.add(underWay);
            });
        }
        return result;
    }

    private void processStatistics(CopStatisticsDto copStatisticsDto) {
        copStatisticsDto.setTotal(copStatisticsDto.getCheck()+ copStatisticsDto.getDelay() + copStatisticsDto.getDelayComplete() + copStatisticsDto.getPlan() + copStatisticsDto.getComplete() + copStatisticsDto.getNoExecute() + copStatisticsDto.getNoExecutePlan() + copStatisticsDto.getUnqualified());
        copStatisticsDto.setCompleteCount(copStatisticsDto.getTotal() -  copStatisticsDto.getNoExecutePlan());
        if(copStatisticsDto.getCompleteCount() != 0) {
            copStatisticsDto.setCompleteRate(NumberCalcUtil.calculatePercentage(copStatisticsDto.getComplete() + copStatisticsDto.getDelayComplete(), copStatisticsDto.getCompleteCount(), true));
        }
    }


    /**
     * 处理每月的排期数据
     * @param name
     * @param copStatisticsDto
     * @param schedules
     * @param monthList
     * @return
     */
    private CopStatisticsDto createStatisticsDto(String name, CopStatisticsDto copStatisticsDto,List<CopSchedule> schedules, List<String> monthList) {
        CopStatisticsDto statisticsDto = new CopStatisticsDto();
        statisticsDto.setName(name);
        statisticsDto.setDeptCode(copStatisticsDto.getDeptCode());
        statisticsDto.setLineCode(copStatisticsDto.getLineCode());
        statisticsDto.setYear(copStatisticsDto.getYear());
        // 初始化其他的计数变量
        int plan = 0;
        int complete = 0;
        int delay = 0;
        int delayComplete = 0;
        int check = 0;
        int noExecute = 0;
        int noExecutePlan = 0;
        int unqualified = 0;
        for (CopSchedule schedule : schedules) {
            List<CopScheduleJson> list = JSONArray.parseArray(schedule.getWeekJson(), CopScheduleJson.class);
            list.removeIf(scheduleJson -> !monthList.contains(String.valueOf(scheduleJson.getMonth())));
            for (CopScheduleJson json : list) {
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
     * 处理每周的排期数据
     * @param name
     * @param schedules
     * @param monthList
     * @return
     */
    private CopPlanRank createWeekStatisticsDto(String name, List<CopSchedule> schedules, List<String> monthList) {
        CopPlanRank statisticsDto = new CopPlanRank();
        if(StringUtils.isNotBlank(name)) {
            statisticsDto.setName(name);
        }
        // 初始化其他的计数变量
        int count = 0;
        int complete = 0;
        for (CopSchedule schedule : schedules) {
            List<CopScheduleJson> list = JSONArray.parseArray(schedule.getWeekJson(), CopScheduleJson.class);
            list.removeIf(scheduleJson -> !monthList.contains(String.valueOf(scheduleJson.getNum())));
            for (CopScheduleJson json : list) {
                if(json.getState().equals("1")) {
                    complete += 1;
                }
                count += 1;
            }
            statisticsDto.setSize(count);
            if(complete != 0) {
                double value = (double) complete / count;
                BigDecimal rate = new BigDecimal(Double.toString(value));
                rate = rate.setScale(2, RoundingMode.HALF_UP);
                statisticsDto.setLv(rate.doubleValue());
            } else {
                statisticsDto.setLv(0.0);
            }
        }
        return statisticsDto;
    }

    /**
     * 处理每周的排期数据
     * @param name
     * @param schedules
     * @param monthList
     * @return
     */
    private CopPlanRank createLineStatisticsDto(String name, List<CopSchedule> schedules, List<String> monthList) {
        CopPlanRank statisticsDto = new CopPlanRank();
        statisticsDto.setName(name);
        // 初始化其他的计数变量
        int plan = 0;
        int complete = 0;
        int delay = 0;
        for (CopSchedule schedule : schedules) {
            List<CopScheduleJson> list = JSONArray.parseArray(schedule.getWeekJson(), CopScheduleJson.class);
            list.removeIf(scheduleJson -> !monthList.contains(String.valueOf(scheduleJson.getNum())));
            for (CopScheduleJson json : list) {
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
}


