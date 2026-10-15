package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.fivewhy.service.IFiveWhyAnalyseService;
import com.keytech.modules.kpi.entity.*;
import com.keytech.modules.kpi.mapper.KpiExceptionSummaryMapper;
import com.keytech.modules.kpi.service.*;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.kpi.util.WeekUtils;
import com.keytech.modules.kpi.vo.ExcPercentVo;
import com.keytech.modules.kpi.vo.Kpi5wAnalyseVo;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @Description: KPI异常清单
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Service
@Slf4j
public class KpiExceptionSummaryServiceImpl extends BaseServiceImpl<KpiExceptionSummaryMapper, KpiExceptionSummary> implements IKpiExceptionSummaryService {
    private static final Logger logger = LoggerFactory.getLogger(KpiExceptionSummaryServiceImpl.class);
    @Value("${mes-system.tenant-id}")
    private Integer tenantId;
    @Autowired
    private IKpiInfoService infoService;
    @Autowired
    private IFiveWhyAnalyseService fiveWhyAnalyseService;
    @Autowired
    private IGapAnalysisService gapAnalysisService;
    @Autowired
    private IKpiReportActualMonthValueService monthValueService;
    @Autowired
    private IKpiReportActualWeekValueService weekValueService;
    @Autowired
    private IMdmWeekService mdmWeekService;
    @Autowired
    private IKpiExceptionSummaryService exceptionSummaryService;
    @Autowired
    private IKpiTypeService kpiTypeService;

    /**
     * 获取清单
     * @param page
     * @param kpiExceptionSummary
     * @return
     */
    @Override
    public IPage<KpiExceptionSummary> queryPageList(Page<KpiExceptionSummary> page, KpiExceptionSummary kpiExceptionSummary) {

        //获取数据
        IPage<KpiExceptionSummary> pageList = page(page, getExceptionQueryWrapper(kpiExceptionSummary));
        List<KpiExceptionSummary> records = pageList.getRecords();

        if(CollUtil.isNotEmpty(records)){
            //数据处理
            pageList(records);
        }
        return pageList;
    }

    /**
     * 获取数据
     * @param kpiExceptionSummary
     */
    private LambdaQueryWrapper<KpiExceptionSummary> getExceptionQueryWrapper(KpiExceptionSummary kpiExceptionSummary) {
        // 查询条件-部门 / KPI类型
        List<String> kpiCodeList = null;
        if(StrUtil.isNotBlank(kpiExceptionSummary.getKpiType()) || StrUtil.isNotBlank(kpiExceptionSummary.getQueryDept())){

            LambdaQueryWrapper<KpiInfo> queryWrapper = new LambdaQueryWrapper<KpiInfo>();
            queryWrapper.eq(StrUtil.isNotBlank(kpiExceptionSummary.getQueryDept()), KpiInfo::getDeptCode,kpiExceptionSummary.getQueryDept());

            String typeId = kpiExceptionSummary.getKpiType();
            if(StrUtil.isNotBlank(typeId)){
                //查询当前类型以及对应的子类,考虑2层层级
                //先查询出所有的kpi类型
                List<KpiType> listType = kpiTypeService.lambdaQuery().and(q-> q.eq(KpiType::getId,typeId).or().eq(KpiType::getParentId,typeId)).list();
                List<String> typeIds = CollStreamUtil.toList(listType,KpiType::getId);
                queryWrapper.in(KpiInfo::getPerentId,typeIds);
            }

            List<KpiInfo> list = infoService.list(queryWrapper);
            //获取kpiCodeList
            kpiCodeList = list.stream().map(KpiInfo::getKpiCode).collect(Collectors.toList());
            if(kpiCodeList != null && kpiCodeList.isEmpty()){
                kpiCodeList.add("-1");
            }
        }
        //时间集合
        List<String > whyTitleList = new ArrayList<>();
        if(StrUtil.isNotBlank(kpiExceptionSummary.getStartTime()) && StrUtil.isNotBlank(kpiExceptionSummary.getEndTime())){
            //获取时间段内月集合 格式 yyyy-MM
            List<String> monthList = DateUtils.getMonthList(kpiExceptionSummary.getStartTime(), kpiExceptionSummary.getEndTime());
            //获取时间段内周 传入 周开始日期 和 周结束日期
            List<MdmWeek> weekList = WeekUtils.getWeekList(kpiExceptionSummary.getStartTime(), kpiExceptionSummary.getEndTime(), mdmWeekService);
            //遍历weekList 得到List<String>集合 拼接 startDate(yyyy-MM-dd)周开始|endDate(yyyy-MM-dd)周结束 , 格式为 yyyy-MM-dd|yyyy-MM-dd
            List<String> weekRangeList = weekList.stream()
                    .map(week -> {
                        String startDate = DateUtils.formatDate(week.getStartDate()); // 确保格式为 yyyy-MM-dd
                        String endDate = DateUtils.formatDate(week.getEndDate());     // 确保格式为 yyyy-MM-dd
                        return startDate + "|" + endDate;
                    })
                    .collect(Collectors.toList());

            whyTitleList.addAll(monthList);
            whyTitleList.addAll(weekRangeList);
        }

        //查询异常清单
        //LambdaQueryWrapper<KpiExceptionSummary> query = getQueryWrapper(kpiExceptionSummary);
        LambdaQueryWrapper<KpiExceptionSummary> query = new LambdaQueryWrapper<>();
        query.orderByDesc(KpiExceptionSummary::getCreateTime);
        query.in(kpiCodeList != null && !kpiCodeList.isEmpty(), KpiExceptionSummary::getKpiCode, kpiCodeList);
        query.in(whyTitleList != null && !whyTitleList.isEmpty(), KpiExceptionSummary::getWhyTitle, whyTitleList);
        return query;
    }


    private LambdaQueryWrapper<KpiExceptionSummary> getQueryWrapper(KpiExceptionSummary kpiExceptionSummary) {
        KpiInfo kpiInfo = new KpiInfo();
        kpiInfo.setOrgCode(kpiExceptionSummary.getHierarchy());
        if (StrUtil.isNotEmpty(kpiExceptionSummary.getKpiCode())) {
            kpiInfo.setPerentId(kpiExceptionSummary.getKpiCode());
        }
        if (Objects.nonNull(kpiExceptionSummary.getKpiType())) {
            kpiInfo.setPerentId(String.valueOf(kpiExceptionSummary.getKpiType()));
        }
        Set<String> kpiCode = CollStreamUtil.toSet(infoService.listKpiInfo(kpiInfo), KpiInfo::getKpiCode);
        LambdaQueryWrapper<KpiExceptionSummary> query = new LambdaQueryWrapper<>();
        query.likeRight(StringUtils.hasLength(kpiExceptionSummary.getFactoryCode()), KpiExceptionSummary::getFactoryCode, kpiExceptionSummary.getFactoryCode());
        if(CollUtil.isNotEmpty(kpiCode)){
            query.in(KpiExceptionSummary::getKpiCode, kpiCode);
        }
        //else{
            //query.in(KpiExceptionSummary::getKpiCode, "-1");
        //}
        if (StringUtils.hasLength(kpiExceptionSummary.getStartTime()) && StringUtils.hasLength(kpiExceptionSummary.getEndTime())) {
            query.between(KpiExceptionSummary::getCreateTime, kpiExceptionSummary.getStartTime(), kpiExceptionSummary.getEndTime());
        }
        return query;
    }



   /**
     * 对KPI异常摘要记录进行分页处理和数据补充
     * @param records KPI异常摘要记录列表
     * @return 处理后的KPI异常摘要记录列表
     */
    public List<KpiExceptionSummary> pageList(List<KpiExceptionSummary> records) {
        if (CollectionUtils.isEmpty(records)) {
            return records;
        }
        // 提取记录中的年份信息并去重，过滤null
        Set<Integer> yearList = records.stream()
                .map(KpiExceptionSummary::getYear)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, String> map = new HashMap<>();
        if (!yearList.isEmpty()){
            //获取周
            LambdaQueryWrapper<MdmWeek> query = new LambdaQueryWrapper<>();
            //年集合
            query.in(MdmWeek::getYear,yearList);
            List<MdmWeek> weekList = mdmWeekService.list(query);
            map = WeekUtils.convertToMap(weekList);
        }



        //5why分析/差距分析
        //获取差距分析
        //遍历 records 得到 Identify 集合
        Set<String> identifySet = records.stream()
                .map(KpiExceptionSummary::getIdentify)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        //获取差距分析
        List<GapAnalysis> gapAnalysisList;
        if (!identifySet.isEmpty()) {
            LambdaQueryWrapper<GapAnalysis> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(GapAnalysis::getRecordId, identifySet);
            wrapper.orderByDesc(GapAnalysis::getCreateTime);
            //差距分析只有月才能有
            gapAnalysisList = gapAnalysisService.list(wrapper);
        } else {
            gapAnalysisList = new ArrayList<>();
        }

        //records KpiCode 集合
        Set<String> kpiCodeSet = records.stream()
                .map(KpiExceptionSummary::getKpiCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        //获取5why数据
        List<FiveWhyAnalyse> listFwhy;
        if (!kpiCodeSet.isEmpty() && !yearList.isEmpty()) {
            LambdaQueryWrapper<FiveWhyAnalyse> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(FiveWhyAnalyse::getKpiCode, kpiCodeSet);
            queryWrapper.in(FiveWhyAnalyse::getKpiYear, yearList);
            listFwhy = fiveWhyAnalyseService.list(queryWrapper);
        } else {
            listFwhy = new ArrayList<>();
        }

        Map<String, String> weekMap = map;

        records.forEach(record -> accept(record, weekMap, gapAnalysisList, listFwhy));
        //获取行动
        records.stream().filter(K -> StringUtils.hasLength(K.getAction())).forEachOrdered(K -> {
            List<Kpi5wAnalyseVo> kpi5wAnalyseVos = JSONArray.parseArray(K.getAction(), Kpi5wAnalyseVo.class);
            if (kpi5wAnalyseVos != null) {
                List<String> list = kpi5wAnalyseVos.stream().filter(a -> !Objects.equals("纠正性措施", a.getAnswer()) || !Objects.equals("预防性措施", a.getAnswer())).map(Kpi5wAnalyseVo::getAnswer).collect(Collectors.toList());
                K.setActionList(list);
            }
        });
        return records;
    }


    /**
     * 5W / 差距分析
     * @param summary
     */
    private void accept(KpiExceptionSummary summary,Map<String, String> weekMap,  List<GapAnalysis> list, List<FiveWhyAnalyse> listFwhy) {
        //差距分析
//        LambdaQueryWrapper<GapAnalysis> wrapper = new LambdaQueryWrapper<>();
        String identify = summary.getIdentify();
//
//        wrapper.eq(GapAnalysis::getRecordId, identify);
//        wrapper.orderByDesc(GapAnalysis::getCreateTime);
        //差距分析只有月才能有
//        List<GapAnalysis> list = gapAnalysisService.list(wrapper);
        if (!isEmpty(list)) {
            //获取list中的 recordId 与 identify 相等的第一条
            GapAnalysis mdmKpi5wAnalyse = list.stream().filter(a -> Objects.equals(a.getRecordId(), identify)).findFirst().orElse(null);
            if (mdmKpi5wAnalyse != null) {
                //GapAnalysis mdmKpi5wAnalyse = list.get(0);
                summary.setDiffType(true);
                summary.setReason(mdmKpi5wAnalyse.getReason());
                summary.setAction(mdmKpi5wAnalyse.getValue());
            }
        }
        //处理周的title 2024-02-01|2024-02-07 月的就是2024-02
        //5why分析
//        LambdaQueryWrapper<FiveWhyAnalyse> queryWrapper = new LambdaQueryWrapper<>();
////        queryWrapper.likeRight(FiveWhyAnalyse::getFactoryCode, summary.getFactoryCode());
//        queryWrapper.eq(FiveWhyAnalyse::getKpiCode, summary.getKpiCode());
////        queryWrapper.eq(FiveWhyAnalyse::getKpiColKey, summary.getWhyTitle());
//        queryWrapper.eq(FiveWhyAnalyse::getKpiYear, summary.getYear());
//        if(StrUtil.isNotBlank(summary.getWhyTitle()) && summary.getWhyTitle().contains("|")){
//            queryWrapper.eq(FiveWhyAnalyse::getKpiColKey, weekMap.get(summary.getWhyTitle()));
//        }else{
//            queryWrapper.eq(FiveWhyAnalyse::getKpiColKey, summary.getWhyTitle());
//        }
//
//        if(weekMap.containsKey(summary.getWhyTitle())){
//            summary.setKpiColKey(weekMap.get(summary.getWhyTitle()));
//
//        }

//        queryWrapper.eq(FiveWhyAnalyse::getKpiYear, dto.getYear())
//                .in(FiveWhyAnalyse::getKpiCode, kpiCodeIds).list();

        //List<FiveWhyAnalyse> listFwhy = fiveWhyAnalyseService.list(queryWrapper);
        //if (!isEmpty(listFwhy)) summary.setFiveType(true);

        List<FiveWhyAnalyse> fwhyList = null;
        if(StrUtil.isNotBlank(summary.getWhyTitle()) && summary.getWhyTitle().contains("|")){
            // listFwhy与 weekMap.get(summary.getWhyTitle())相同对象
            fwhyList = listFwhy.stream().filter(a -> Objects.equals(a.getKpiColKey(), weekMap.get(summary.getWhyTitle()))).collect(Collectors.toList());
            summary.setKpiColKey(weekMap.get(summary.getWhyTitle()));
        }else{
            fwhyList = listFwhy.stream().filter(a -> Objects.equals(a.getKpiColKey(), summary.getWhyTitle())).collect(Collectors.toList());
        }
        if (!isEmpty(fwhyList)) summary.setFiveType(true);

    }


    /**
     * 统计
     * @param kpiExceptionSummary
     * @return
     */
    @Override
    public ExcPercentVo percent(KpiExceptionSummary kpiExceptionSummary) {
        LambdaQueryWrapper<KpiExceptionSummary> query = getExceptionQueryWrapper(kpiExceptionSummary);
        List<KpiExceptionSummary> list = list(query);
        if (CollectionUtils.isEmpty(list)) {
            return new ExcPercentVo();
        }
        //补充数据(5wyh/差距分析)
        pageList(list);
        ExcPercentVo vo = new ExcPercentVo();
        int size = list.size();
        vo.setExcSum(size);
        int count;
        if (kpiExceptionSummary.getType()) {
            count = (int) list.stream().filter(KpiExceptionSummary::getFiveType).count();
        } else {
            count = (int) list.stream().filter(KpiExceptionSummary::getDiffType).count();
        }
        if (kpiExceptionSummary.getIsAllType()) {
            count = (int) list.stream().filter(bean -> bean.getDiffType() || bean.getFiveType()).count();
        }
        vo.setExcCount(count);
        double v = (double) count / (double) size;
        vo.setPercent(v);
        return vo;
    }

    /**
     * KPI异常清单[定时任务]
     * @return
     */
    @Override
    public void synchronizeMonthWeekValueJob() {
        logger.info("开始同步周/月数据异常清单");
        // 生成月度异常清单
        syncMonthData();
        // 生成周度异常清单
        syncWeekData();
        logger.info("同步周/月数据异常清单结束");
    }

    /**
     * 生成月度异常清单
     */
    private void syncMonthData() {
        Date oneMonthAgo = DateUtil.offsetMonth(new Date(), -1);
        String formattedDate = DateUtils.DATETIME_FORMAT.format(oneMonthAgo);

        LambdaQueryWrapper<KpiReportActualMonthValue> query = new LambdaQueryWrapper<>();
        query.gt(KpiReportActualMonthValue::getCreateTime, formattedDate)
                .in(KpiReportActualMonthValue::getColour, 2, 3) //颜色
                .orderByDesc(KpiReportActualMonthValue::getCreateTime)
                .eq(KpiReportActualMonthValue::getIndicator, 1); //月
        //获取月
        List<KpiReportActualMonthValue> monthValues = monthValueService.list(query);
        processValues(monthValues, true);
        //清理colour已恢复正常的异常清单
        cleanupRecoveredSummaries(monthValues, formattedDate, true);
    }

    /**
     * 生成周度异常清单
     */
    private void syncWeekData() {
//        Date oneWeekAgo = DateUtil.offsetDay(new Date(), -15);
        Date oneWeekAgo = DateUtil.offsetDay(new Date(), -30);
        String formattedDate = DateUtils.DATETIME_FORMAT.format(oneWeekAgo);

        LambdaQueryWrapper<KpiReportActualWeekValue> query = new LambdaQueryWrapper<>();
        query.gt(KpiReportActualWeekValue::getCreateTime, formattedDate)
                .in(KpiReportActualWeekValue::getColour, 2, 3)
                .orderByDesc(KpiReportActualWeekValue::getCreateTime);

        List<KpiReportActualWeekValue> weekValues = weekValueService.list(query);
        processValues(weekValues, false);
        //清理colour已恢复正常的异常清单
        cleanupRecoveredSummaries(weekValues, formattedDate, false);
    }

    /**
     * 清理colour已恢复正常的异常清单数据
     * 当colour从2/3变为1时，对应的异常清单记录需要删除
     *
     * @param values        当前仍为异常（colour=2/3）的记录列表
     * @param formattedDate 时间范围的格式化日期字符串
     * @param isMonth       是否为月度数据
     * @param <T>           数据类型
     */
    private <T> void cleanupRecoveredSummaries(List<T> values, String formattedDate, boolean isMonth) {
        if (CollUtil.isEmpty(values)) {
            return;
        }
        //收集当前仍为异常（colour=2/3）的记录ID，过滤掉null
        List<String> activeIdentifies = values.stream()
                .map(value -> isMonth ? ((KpiReportActualMonthValue) value).getId() : ((KpiReportActualWeekValue) value).getId())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(activeIdentifies)) {
            return;
        }

        //查询同一时间范围、同一类型的异常清单中，identify不在当前异常列表中的记录
        LambdaQueryWrapper<KpiExceptionSummary> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(KpiExceptionSummary::getCreateTime, formattedDate)
                .eq(KpiExceptionSummary::getIdentifyType, isMonth ? 1 : 0)
                .notIn(KpiExceptionSummary::getIdentify, activeIdentifies);

        //删除colour已恢复正常的异常清单
        exceptionSummaryService.remove(wrapper);
    }


    /**
     * 处理KPI数据并生成异常汇总信息。
     *
     * @param values KPI数据列表
     * @param isMonth 是否为月度数据
     * @param <T>     数据类型
     */
    private <T> void processValues(List<T> values, boolean isMonth) {
        String year = Calendar.getInstance().get(Calendar.YEAR)+"";
        //KPI异常清单
        List<KpiExceptionSummary> summaries = new ArrayList<>();
        //获取周
        Map<Integer, String> weekOfYear = isMonth ? null : mdmWeekService.getWeekOfYear(year);

        for (T value : values) {
            //获取工厂Code
            String factory = isMonth ? ((KpiReportActualMonthValue) value).getFactoryCode() : ((KpiReportActualWeekValue) value).getFactoryCode();
            //获取KPI KpiCode-->KpiInfo对象
            Map<String, KpiInfo> kpiInfoMap = infoService.kpiNameMapping(factory, null, year);
            //KPI异常清单(isMonth为true 月度数据 false周)
            KpiExceptionSummary summary = isMonth ? createKpiExceptionSummary((KpiReportActualMonthValue) value, kpiInfoMap) :
                    createKpiExceptionSummary((KpiReportActualWeekValue) value, kpiInfoMap, weekOfYear);
            if (summary != null) {
                summaries.add(summary);
            }
        }
        //月度-KPI异常分析数据存储
        exceptionSummaryService.saveOrUpdateBatch(summaries);
    }


    /**
     * 根据KPI月度实际值创建KPI异常汇总信息。
     *
     * @param value       KPI月度实际值对象
     * @param kpiInfoMap  KPI信息映射表
     * @return KPI异常汇总信息对象，若KPI信息不存在则返回null
     */
    private KpiExceptionSummary createKpiExceptionSummary(KpiReportActualMonthValue value, Map<String, KpiInfo> kpiInfoMap) {
        //KPI
        KpiInfo info = kpiInfoMap.get(value.getKpiCode());
        if (info == null) return null;

        KpiExceptionSummary summary = new KpiExceptionSummary();
        //月-异常清单赋值
        populateCommonFields(summary, value, info);
        summary.setIdentifyType(1);
        summary.setWhyTitle(DateUtil.format(value.getStartTime(), "yyyy-MM"));
        //生成标题-所属周/月/年
        summary.setDateIdentify(formatMonthDateIdentify(value));
        //年
        summary.setYear(value.getYear());
        //住户
        summary.setTenantId(tenantId);
        //填充KPI异常描述信息。
        populateAnomalyDescription(summary, info, value.getExtJson(), value.getValue());
        ///获取异常清单ID
        setExistingIdIfExists(summary);

        return summary;
    }


    /**
     * 根据KPI周度实际值创建KPI异常汇总信息。
     *
     * @param value       KPI周度实际值对象
     * @param kpiInfoMap  KPI信息映射表
     * @param weekOfYear  周度与年份的映射关系
     * @return KPI异常汇总信息对象，若KPI信息不存在则返回null
     */
    private KpiExceptionSummary createKpiExceptionSummary(KpiReportActualWeekValue value, Map<String, KpiInfo> kpiInfoMap, Map<Integer, String> weekOfYear) {
        KpiInfo info = kpiInfoMap.get(value.getKpiCode());
        if (info == null) return null;

        KpiExceptionSummary summary = new KpiExceptionSummary();
        //周-异常清单赋值
        populateCommonFields(summary, value, info);
        summary.setIdentifyType(0);
        summary.setWhyTitle(DateUtil.formatDate(value.getStartTime()) + "|" + DateUtil.formatDate(value.getEndTime()));
        //生成标题-所属周/月/年
        summary.setDateIdentify(formatWeekDateIdentify(value, weekOfYear));
        //年
        summary.setYear(value.getYear());
        //住户
        summary.setTenantId(tenantId);
        //异常情况描述
        populateAnomalyDescription(summary, info, value.getExtJson(), value.getValue());
        //获取异常清单ID
        setExistingIdIfExists(summary);

        return summary;
    }

    /**
     * 为KPI异常汇总信息赋值通用字段。
     *
     * @param summary KPI异常汇总信息对象
     * @param value   KPI月度实际值对象
     * @param info    KPI信息对象
     */
    private void populateCommonFields(KpiExceptionSummary summary, KpiReportActualMonthValue value, KpiInfo info) {
        summary.setFactoryCode(value.getFactoryCode());
        summary.setIdentify(value.getId());
        summary.setKpiCode(value.getKpiCode());
        summary.setKpiName(info.getKpiName());
        summary.setKpiType(info.getPerentId());
        summary.setColor(value.getColour());
        summary.setCreateTime(value.getCreateTime());
    }


    /**
     * 为周度KPI异常汇总信息赋值通用字段。
     *
     * @param summary KPI异常汇总信息对象
     * @param value   KPI周度实际值对象
     * @param info    KPI信息对象
     */
    private void populateCommonFields(KpiExceptionSummary summary, KpiReportActualWeekValue value, KpiInfo info) {
        summary.setFactoryCode(value.getFactoryCode());
        summary.setIdentify(value.getId());
        summary.setKpiCode(value.getKpiCode());
        summary.setKpiName(info.getKpiName());
        summary.setKpiType(info.getPerentId());
        summary.setColor(value.getColour());
        summary.setCreateTime(value.getCreateTime());
    }

    /**
     * 格式化月度日期标识。(生成标题-所属周/月/年)
     *
     * @param value KPI月度实际值对象
     * @return 格式化后的日期标识字符串，若年份为空则返回null
     */
    private String formatMonthDateIdentify(KpiReportActualMonthValue value) {
        if (value.getYear() != null) {
            String month = String.valueOf(value.getMonth());
            return value.getYear() + "年第" + month.replace(value.getYear().toString(), "") + "月";
        }
        return null;
    }

    /**
     * 格式化周度日期标识。(生成标题-所属周/月/年)
     *
     * @param value     KPI周度实际值对象
     * @param weekOfYear 周度与年份的映射关系
     * @return 格式化后的日期标识字符串，若年份为空则返回null
     */
    private String formatWeekDateIdentify(KpiReportActualWeekValue value, Map<Integer, String> weekOfYear) {
        if (value.getYear() != null) {
            //int indexByWeek = WeekUtils.getIndexByWeek(DateUtil.formatDate(value.getStartTime())+ "|" + DateUtil.formatDate(value.getEndTime()), weekOfYear);
            return value.getYear() + "年第" + value.getWeekNumber() + "周";
        }
        return null;
    }

    /**
     * 填充KPI异常描述信息。
     *
     * @param summary      KPI异常汇总信息对象
     * @param info         KPI信息对象
     * @param extJson      扩展JSON字符串
     * @param actualValue  实际值字符串
     */
    private void populateAnomalyDescription(KpiExceptionSummary summary, KpiInfo info, String extJson, String actualValue) {
        if (StringUtils.hasLength(extJson)) {
            JSONObject resultData = JSONObject.parseObject(extJson);
            //差值
            Object diffValue = resultData.get("diffValue");
            String bj = "未知";
            String result = "未知";
            if (diffValue != null) {
                //bj = (Double.parseDouble(diffValue.toString()) > Double.parseDouble(actualValue)) ? "高于" : "低于";
                bj = Double.parseDouble(diffValue.toString()) > 0?"高于" : "低于";
                //计算实际值
                BigDecimal actualNum = new BigDecimal(actualValue); //实际值
                BigDecimal diffNum = new BigDecimal(diffValue.toString()); //差值
                BigDecimal targetValue = actualNum.subtract(diffNum); //(目标值)实际值-差值
                result = targetValue.toString();
            } else {
                diffValue = "未知";
            }
            String color = (summary.getColor() == 2) ? "红区" : "黄区";

            summary.setAnomalyDescription(info.getKpiName() + " 目标值为 " + result + " 实际值为 " + actualValue
                    + " 此KPI指标为 " + bj + "目标值合格 结果为" + color);
        }
    }

    /**
     * 如果存在已有的KPI异常汇总信息，则设置其ID。
     *
     * @param summary KPI异常汇总信息对象
     */
    private void setExistingIdIfExists(KpiExceptionSummary summary) {
        List<KpiExceptionSummary> data = exceptionSummaryService.list(new LambdaQueryWrapper<KpiExceptionSummary>()
                .eq(KpiExceptionSummary::getIdentify, summary.getIdentify())
                .last("LIMIT 1"));
        if (!data.isEmpty()) {
            summary.setId(data.get(0).getId());
        }
    }
}
