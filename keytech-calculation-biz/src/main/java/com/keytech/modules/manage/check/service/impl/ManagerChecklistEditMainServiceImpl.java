package com.keytech.modules.manage.check.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.manage.check.entity.*;
import com.keytech.modules.manage.check.mapper.ManagerChecklistEditMainMapper;
import com.keytech.modules.manage.check.service.*;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.entity.MdmPost;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.mdm3.service.IMdmPostService;
import org.jeecg.common.system.vo.DictModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 分层检查-检查清单录入主表
 * @Author: jeecg-boot
 * @Date:   2025-04-10
 * @Version: V1.0
 */
@Service
public class ManagerChecklistEditMainServiceImpl extends BaseServiceImpl<ManagerChecklistEditMainMapper, ManagerChecklistEditMain> implements IManagerChecklistEditMainService {
    @Autowired
    private IManagerCheckConfigService managerCheckConfigService;
    @Autowired
    private IManagerChecklistEditRecordService iManagerChecklistEditRecordService;
    @Autowired
    private IManageCheckListService manageCheckListService;
    @Autowired
    private IMdmDictService mdmDictService;
    @Autowired
    private IMdmPostService mdmPostService;
    @Autowired
    private IManageCheckCategoryService manageCheckCategory;
    @Autowired
    private IManagerChecklistEditMainService managerChecklistEditMainService;
    @Autowired
    private IManagerChecklistEditRecordService managerChecklistEditRecordService;

    @Override
    public Map<String, Object> queryInspectData(ManagerChecklistEditMain managerChecklistEditMian) {
        //返回集合
        List<ManagerChecklistEditRecord> listAll = new ArrayList<>();
        ManagerChecklistEditMain mainData = new ManagerChecklistEditMain();

        //管理者检查清单-频率、体系、检查来源字典
        List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
                List.of(
                        "MES3_MANAGE_CHECK_SOURCE"
                        , "MES3_MANAGE_CHECK_TX"
                        , "MES3_MANAGE_CHECK_PL"
                )
        );
        //频率字典
        Map<String, String> frequencyMap = mdmDictList.stream()
                .filter(item -> "MES3_MANAGE_CHECK_PL".equals(item.getGroupCode()))
                .collect(Collectors.toMap(
                        MdmDict::getItemVal,
                        MdmDict::getItemKey,
                        (existing, replacement) -> existing)
                );
        //体系字典
        Map<String, String> systemMap = mdmDictList.stream()
                .filter(item -> "MES3_MANAGE_CHECK_TX".equals(item.getGroupCode()))
                .collect(Collectors.toMap(
                        MdmDict::getItemVal,
                        MdmDict::getItemKey,
                        (existing, replacement) -> existing)
                );
        //检查来源字典
        Map<String, String> sourceMap = mdmDictList.stream()
                .filter(item -> "MES3_MANAGE_CHECK_SOURCE".equals(item.getGroupCode()))
                .collect(Collectors.toMap(
                        MdmDict::getItemVal,
                        MdmDict::getItemKey,
                        (existing, replacement) -> existing)
                );

        //获取填报项目
        if(StrUtil.isNotBlank(managerChecklistEditMian.getId())){
            //修改获取历史填报项目
            //主数据
            mainData = getById(managerChecklistEditMian.getId());
            //明细
            LambdaQueryWrapper<ManagerChecklistEditRecord> queryDeptWrapper = new LambdaQueryWrapper<ManagerChecklistEditRecord>();
            queryDeptWrapper.eq(ManagerChecklistEditRecord::getMainId, managerChecklistEditMian.getId());
            queryDeptWrapper.eq(ManagerChecklistEditRecord::getDelFlag, 0);
            if("1".equals(managerChecklistEditMian.getStopProduction())){
                //是否停产 未停产 true
                //排序 生产下执行的排在最上面(不可编辑)
                queryDeptWrapper.orderByAsc(ManagerChecklistEditRecord::getNumber);
            }else{
                //排序 不在生产下执行排在最上面(可编辑)
                queryDeptWrapper.orderByAsc(ManagerChecklistEditRecord::getStopProduction).orderByAsc(ManagerChecklistEditRecord::getNumber);
            }
            List<ManagerChecklistEditRecord> list = iManagerChecklistEditRecordService.list(queryDeptWrapper);
            int num = 0;
            for (ManagerChecklistEditRecord managerChecklistEditRecordData : list) {
                num++;
                for (int i = 1; i < 6; i++) {
                    ManagerChecklistEditRecord managerChecklistEditRecord = new ManagerChecklistEditRecord();
                    BeanUtil.copyProperties(managerChecklistEditRecordData, managerChecklistEditRecord);
                    //序号
                    managerChecklistEditRecord.setNumber(num);
                    //检查项来源
                    managerChecklistEditRecord.setSourceName(sourceMap.get(managerChecklistEditRecordData.getSource()));
                    //频率
                    managerChecklistEditRecord.setFrequencyName(frequencyMap.get(managerChecklistEditRecordData.getFrequency()));
                    //体系
                    managerChecklistEditRecord.setSystemName(systemMap.get(managerChecklistEditRecordData.getSystemInfo()));
                    //类型(1检查记录 2检查图片 3检查判定 4异常描述 5异常提交)
                    managerChecklistEditRecord.setRowType(i);
                    listAll.add(managerChecklistEditRecord);
                }
            }
        }else {
            //新增从配置项中获取
            LambdaQueryWrapper<ManagerCheckConfig> queryDeptWrapper = new LambdaQueryWrapper<ManagerCheckConfig>();
            queryDeptWrapper.eq(ManagerCheckConfig::getDelFlag, 0);
            //部门
            if(StrUtil.isNotBlank(managerChecklistEditMian.getDeptCode())){
                if(managerChecklistEditMian.getDeptCode().length() < 12){
                    queryDeptWrapper.likeRight(ManagerCheckConfig::getDeptCode, managerChecklistEditMian.getDeptCode());
                } else {
                    queryDeptWrapper.eq(ManagerCheckConfig::getDeptCode, managerChecklistEditMian.getDeptCode());
                }
            }
            if("1".equals(managerChecklistEditMian.getStopProduction())){
                //是否停产 生产 true
                //排序 生产下执行的排在最上面(不可编辑)
                queryDeptWrapper.orderByAsc(ManagerCheckConfig::getSort);
            }else{
                //排序 停产且不在生产下执行排[否]排最上面(可编辑)  [是]不可编辑排最下
                queryDeptWrapper.orderByAsc(ManagerCheckConfig::getIfZx).orderByAsc(ManagerCheckConfig::getSort);
            }

            List<ManagerCheckConfig> list = managerCheckConfigService.list(queryDeptWrapper);

            //一条数据分成5个项目
            int num = 0;
            for (ManagerCheckConfig ManagerCheckConfig : list) {
                //过滤无效数据
                //岗位id
                String postId = managerChecklistEditMian.getPostId();
                String pushWeek = managerChecklistEditMian.getPushWeek();
                //岗位 星期配置
                String json = ManagerCheckConfig.getGwpzJson();

                if(StrUtil.isNotBlank(json) && StrUtil.isNotBlank(postId) && StrUtil.isNotBlank(pushWeek)){
                    //JSONObject jsonObject = JSONUtil.parseObj(json);
                    JSONArray jsonArray = JSONUtil.parseArray(json);
                    //频率
                    String pl ="";
                    //是否有效, 有效加入检查清单
                    Boolean isEffective = false;
                    for (Object object : jsonArray) {
                        JSONObject jsonObject = (JSONObject) object;
                        String weeks = jsonObject.get("week")+"";
                        //判断岗位是否相同, 判断星期是否相同或者频率为每天
                        if(postId.equals(jsonObject.get("id")+"")){
                            if(weeks.contains(pushWeek) || "90".equals(jsonObject.get("pl"))){
                                pl = jsonObject.get("pl")+"";
                                isEffective = true;
                                break;
                            }
                        }
                    }
                    if(!isEffective){
                        //无效不添加
                        continue;
                    }

                    //序号
                    num++;
                    for (int i = 1; i < 6; i++) {
                        ManagerChecklistEditRecord managerChecklistEditRecord = new ManagerChecklistEditRecord();
                        //配置表id
                        managerChecklistEditRecord.setConfigurationId(ManagerCheckConfig.getId());

                        //序号
                        managerChecklistEditRecord.setNumber(num);
                        //检查项编号
                        managerChecklistEditRecord.setItemNumber(ManagerCheckConfig.getCheckBh());
                        //常规活动
                        managerChecklistEditRecord.setActivities(ManagerCheckConfig.getCghd());
                        //检查项来源
                        managerChecklistEditRecord.setSource(ManagerCheckConfig.getJcxlyDm());
                        managerChecklistEditRecord.setSourceName(sourceMap.get(ManagerCheckConfig.getJcxlyDm()));
                        //频率
                        managerChecklistEditRecord.setFrequency(pl);
                        managerChecklistEditRecord.setFrequencyName(frequencyMap.get(pl));
                        //体系
                        managerChecklistEditRecord.setSystemInfo(ManagerCheckConfig.getJctxDm());
                        managerChecklistEditRecord.setSystemName(systemMap.get(ManagerCheckConfig.getJctxDm()));
                        //检查内容
                        managerChecklistEditRecord.setContent(ManagerCheckConfig.getJcnr());
                        //是否停产(是否生产推送) 1是(true) 0否(false)
                        managerChecklistEditRecord.setStopProduction(ManagerCheckConfig.getIfZx());

                        //类型(1检查记录 2检查图片 3检查判定 4异常描述 5异常提交)
                        managerChecklistEditRecord.setRowType(i);
                        if(i == 3){
                            //判断ok/nok
                            managerChecklistEditRecord.setSure("true");
                        }
                        listAll.add(managerChecklistEditRecord);
                    }
                }

            }
        }

        Map<String, Object> map = new HashMap();
        map.put("itemList",listAll);//项目
        map.put("mainData",mainData); //主数据(填报页面顶部)
        return map;
    }


    /**
     * 数据存储
     * @param managerChecklistEditMain
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void dataSave(ManagerChecklistEditMain managerChecklistEditMain) {
        //保存主表
        //是否编辑
        saveOrUpdate(managerChecklistEditMain);
        //检查项目
        List<ManagerChecklistEditRecord> managerChecklistEditRecordList = managerChecklistEditMain.getManagerChecklistEditRecordList();
        //数据合并
        //合并后集合
        List<ManagerChecklistEditRecord> managerChecklistEditRecordListNew = new ArrayList<>();
        //按配置表id分组
        Map<String, List<ManagerChecklistEditRecord>> groupedRecordMap = managerChecklistEditRecordList.stream()
                .collect(Collectors.groupingBy(ManagerChecklistEditRecord::getConfigurationId));
        List<String> manageChecIds = new ArrayList<>();
        //遍历分组 5条 合并成一条
        for (Map.Entry<String, List<ManagerChecklistEditRecord>> entry : groupedRecordMap.entrySet()) {
            List<ManagerChecklistEditRecord> items = entry.getValue();
            ManagerChecklistEditRecord managerChecklistEditRecord = null;
            for (ManagerChecklistEditRecord item : items) {
                if(null == managerChecklistEditRecord){
                    managerChecklistEditRecord = item;
                    //设置主表id
                    managerChecklistEditRecord.setMainId(managerChecklistEditMain.getId());
                }
                if (1 == item.getRowType()) {
                    managerChecklistEditRecord.setRecord(item.getRecord());
                } else if (2 == item.getRowType()) {
                    managerChecklistEditRecord.setPicture(item.getPicture());
                }else if (3 == item.getRowType()) {
                    managerChecklistEditRecord.setSure(item.getSure());
                }else if (4 == item.getRowType()) {
                    managerChecklistEditRecord.setDescription(item.getDescription());
                }else if (5 == item.getRowType()) {
                    ManageCheckList checkList = new ManageCheckList();
                    manageChecIds.add(item.getLayeringId());
                    managerChecklistEditRecord.setLayeringId(item.getLayeringId());
                }
            }
            if(null != managerChecklistEditRecord){
                managerChecklistEditRecordListNew.add(managerChecklistEditRecord);
            }
        }
        manageCheckListService.update(new LambdaUpdateWrapper<ManageCheckList>().set(ManageCheckList::getDelFlag, 0).in(ManageCheckList::getId, manageChecIds));
        iManagerChecklistEditRecordService.saveOrUpdateBatch(managerChecklistEditRecordListNew);
    }


    /**
     * 检查清单定时推送
     * HQ
     * 2025年4月15日
     *
     * 管理者检查清单自动推送(月/季/半/年)
     * 1.获取管理岗(管理岗)
     * 2.获取清单配置
     * 3.获取(月/季/半/年)与岗位是否匹配
     * 4.匹配的生成检查清单
     *
     * 推送逻辑
     * 1次/月：每月1号推送，名称为xx（岗位名称）月度检查清单
     * 1次/3月：1、4、7、10月1号推送，名称为xx（岗位名称）季度检查清单
     * 1次/6月：1、7月1号推送，名称为xx（岗位名称）半年度检查清单
     * 1次/年：1月1号推送，名称为xx（岗位名称）年度检查清单
     */
    @Override
    public void managerMonthYearCheckPush() {
        try {
            //1.获取管理岗(管理岗)
            LambdaQueryWrapper<MdmPost> queryDeptWrapper = new LambdaQueryWrapper<MdmPost>();
            //岗位类型 管理岗_1
            queryDeptWrapper.eq(MdmPost::getPostType,"1");
            List<MdmPost> classPostList =  mdmPostService.list(queryDeptWrapper);

            //2.获取清单配置
            LambdaQueryWrapper<ManagerCheckConfig> queryDeptWrapperConfiguration = new LambdaQueryWrapper<ManagerCheckConfig>();
            queryDeptWrapperConfiguration.orderByAsc(ManagerCheckConfig::getSort);
            List<ManagerCheckConfig> configurationList = managerCheckConfigService.list(queryDeptWrapperConfiguration);

            //获取当前是星期几
            String pushWeek = getWeek();

            //推送集合
            List<ManagerChecklistEditMain> managerChecklistEditMainList = new ArrayList<>();
            //3.获取(月季半年)与岗位是否匹配
            for (MdmPost mdmClassPost : classPostList) {
                //岗位id
                String postId = mdmClassPost.getId()+"";
                //推送主表
                ManagerChecklistEditMain managerChecklistEditMain = new ManagerChecklistEditMain();
                managerChecklistEditMain.setDeptCode(mdmClassPost.getDeptCode());
                managerChecklistEditMain.setPushWeek(pushWeek);
                managerChecklistEditMain.setRecordDate(new Date());
                managerChecklistEditMain.setRecordWeek(pushWeek);
                managerChecklistEditMain.setIsEdit("0");
                //生产状态(1)
                managerChecklistEditMain.setStopProduction("1");
                managerChecklistEditMain.setPostId(postId);
                //租户
                managerChecklistEditMain.setTenantId(mdmClassPost.getTenantId());
                //工厂
                managerChecklistEditMain.setFactoryCode(mdmClassPost.getFactoryCode());

                //检查明细(清单)
                List<ManagerChecklistEditRecord> managerChecklistEditRecordList = new ArrayList<>();
                int num = 0;
                for (ManagerCheckConfig managerCheckConfig : configurationList) {
                    //过滤无效数据

                    //岗位 条件配置(pl 90,91,92,93,94)
                    String json = managerCheckConfig.getGwpzJson();
                    if(StrUtil.isNotBlank(json) && StrUtil.isNotBlank(postId)) {
                        JSONArray jsonArray = JSONUtil.parseArray(json);
                        //频率
                        String pl = "";
                        //是否有效, 有效加入检查清单
                        Boolean isEffective = false;
                        for (Object object : jsonArray) {
                            JSONObject jsonObject = (JSONObject) object;
                            String weeks = jsonObject.get("week") + "";
                            pl = jsonObject.get("pl") + "";
                            //判断岗位是否相同, 判断是否满足条件
                            if (postId.equals(jsonObject.get("id") + "") && (getMothYear(pl, pushWeek, jsonObject) ||
                                    ((pl.equals("1") || pl.equals("2")) && weeks.contains(pushWeek)))) {
                                //设置推送频率
                                //managerChecklistEditMain.setFrequency(pl);
                                isEffective = true;
                                break;
                            }
                        }
                        if (!isEffective) {
                            //无效不添加
                            continue;
                        }
                        num++;
                        ManagerChecklistEditRecord managerChecklistEditRecord = new ManagerChecklistEditRecord();
                        //配置表id
                        managerChecklistEditRecord.setConfigurationId(managerCheckConfig.getId());
                        //序号
                        managerChecklistEditRecord.setNumber(num);
                        //检查项编号
                        managerChecklistEditRecord.setItemNumber(managerCheckConfig.getCheckBh());
                        //常规活动
                        managerChecklistEditRecord.setActivities(managerCheckConfig.getCghd());
                        //检查项来源
                        managerChecklistEditRecord.setSource(managerCheckConfig.getJcxlyDm());
                        //频率
                        managerChecklistEditRecord.setFrequency(pl);
                        //体系
                        managerChecklistEditRecord.setSystemInfo(managerCheckConfig.getJctxDm());
                        //检查内容
                        managerChecklistEditRecord.setContent(managerCheckConfig.getJcnr());
                        //是否停产(是否生产推送) 1是(true) 0否(false)
                        managerChecklistEditRecord.setStopProduction(managerCheckConfig.getIfZx());
                        //判断ok/nok
                        managerChecklistEditRecord.setSure("true");
                        //租户
                        managerChecklistEditRecord.setTenantId(managerCheckConfig.getTenantId());
                        //工厂
                        managerChecklistEditRecord.setFactoryCode(managerCheckConfig.getFactoryCode());

                        managerChecklistEditRecordList.add(managerChecklistEditRecord);
                    }
                }

                if(null != managerChecklistEditRecordList && managerChecklistEditRecordList.size() > 0){
                    managerChecklistEditMain.setManagerChecklistEditRecordList(managerChecklistEditRecordList);
                    managerChecklistEditMainList.add(managerChecklistEditMain);
                }
            }
            //4.匹配的生成检查清单
            for (ManagerChecklistEditMain managerChecklistEditMain : managerChecklistEditMainList) {
                managerChecklistEditMainService.save(managerChecklistEditMain);
            }
            // 将外层对象的id赋给里层对象的mainId
            managerChecklistEditMainList.forEach(outer -> outer.getManagerChecklistEditRecordList().forEach(inner -> inner.setMainId(outer.getId())));

            //清单集合
            List<ManagerChecklistEditRecord> managerChecklistEditRecordAll = new ArrayList<>();
            for (ManagerChecklistEditMain managerChecklistEditMain : managerChecklistEditMainList) {
                managerChecklistEditRecordAll.addAll(managerChecklistEditMain.getManagerChecklistEditRecordList());
            }
            managerChecklistEditRecordService.saveBatch(managerChecklistEditRecordAll);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 星期一用1表示，星期日用7表示
     * 获取当前周
     * @return
     */
    public String getWeek(){
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // 在Java中，Calendar.SUNDAY的值是1，Calendar.MONDAY的值是2，依此类推。
        // 这里将其转换为题目要求的格式，即星期一用1表示，星期日用7表示
        if (dayOfWeek == Calendar.SUNDAY) {
            dayOfWeek = 7;
        } else {
            dayOfWeek--;
        }
        return dayOfWeek+"";
    }

    /**
     * 判断是否满足月/季/半/年
     */
    public Boolean getMothYear(String type,String week, JSONObject configObject){
        if("90".equals(type)){
            //每天推送(排除周六/周天)
            return !"6".equals(week) && !"7".equals(week);
        } else {
            //如果有配置日期为新，否则使用旧配置判断
            if(configObject.containsKey("monDay1")){
                return compare(configObject);
            } else {
                // 获取当前日期
                LocalDate currentDate = LocalDate.now();
                int dayOfMonth = currentDate.getDayOfMonth();
                int month = currentDate.getMonthValue();
                switch (type) {
                    case "91", "89":
                        //如果是每月推送，且有天数配置为新配置
                        if(configObject.containsKey("day")){
                            String day = configObject.getStr("day");
                            if (day != null) {
                                String[] dayArr = day.split( ",");
                                for(int i = 0; i < day.split( ",").length; i++){
                                    String formatted = String.format("%02d-%02d", month, Integer.valueOf(dayArr[i]));
                                    configObject.set("monDay"+(i+1), formatted);
                                }
                            }
                            return compare(configObject);
                        } else {
                            //旧配置：1次/月 每月 1 号
                            return dayOfMonth == 1;
                        }
                    case "92":
                        //1次/3月 1、4、7、10 月 1 号
                        return dayOfMonth == 1 && (month == 1 || month == 4 || month == 7 || month == 10);
                    case "93":
                        //1次/6月 1、7 月 1 号
                        return dayOfMonth == 1 && (month == 1 || month == 7);
                    case "94":
                        //1次/年 1 月 1 号
                        return dayOfMonth == 1 && month == 1;
                    default:
                        return false;
                }
            }
        }
    }

    public Boolean compare(JSONObject configObject){
        //格式化日期
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd");
        // 配置对象中的键
        String[] objKeys = {"monDay1", "monDay2", "monDay3", "monDay4"};
        // 获取当前日期
        LocalDate localDate = LocalDate.now();
        String currentDate = localDate.format(dtf);
        //判断配置中的第一个日期是否为空，如果为空则获取当月的第一天，用于兼容历史配置
        if(!configObject.containsKey(objKeys[0])){
            // 获取当月的第一天
            LocalDate firstDayOfMonth = localDate.with(TemporalAdjusters.firstDayOfMonth());
            configObject.set(objKeys[0], firstDayOfMonth.format(dtf));
        }
        for (String objKey : objKeys) {
            if (configObject.containsKey(objKey) && currentDate.equals(configObject.getStr(objKey))) {
                return true;
            }
        }
        return false;
    }
}
