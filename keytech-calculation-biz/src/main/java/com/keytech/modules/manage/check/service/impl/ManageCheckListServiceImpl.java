package com.keytech.modules.manage.check.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.service.IActionPlanProblemTypeService;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.common.GetDeptChildUtil;
import com.keytech.modules.home.dto.StatisticalVo;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.manage.check.dto.ManageCheckListDto;
import com.keytech.modules.manage.check.dto.ManageCheckListSummaryDto;
import com.keytech.modules.manage.check.entity.ManageCheckCategory;
import com.keytech.modules.manage.check.entity.ManageCheckList;
import com.keytech.modules.manage.check.mapper.ManageCheckListMapper;
import com.keytech.modules.manage.check.service.IManageCheckCategoryService;
import com.keytech.modules.manage.check.service.IManageCheckListService;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.entity.MdmPost;
import com.keytech.modules.mdm3.entity.MdmShiftGroup;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.mdm3.service.IMdmPostService;
import com.keytech.modules.mdm3.service.IMdmShiftGroupService;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * &#064;Description:  管理者检查清单-分层检查清单
 * &#064Author: jeecg-boot
 * &#064Date:   2025-03-31
 * &#064Version: V1.0
 */
@Service
public class ManageCheckListServiceImpl extends BaseServiceImpl<ManageCheckListMapper, ManageCheckList> implements IManageCheckListService {

    @Autowired
    private IActionPlanTrackService actionPlanTrackService;
    @Autowired
    private IActionPlanProblemTypeService actionPlanProblemTypeService;

    @Autowired
    private IMdmDictService mdmDictService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysDepartService sysDepartService;

    @Autowired
    private IManageCheckCategoryService manageCheckCategoryService;

    @Autowired
    private IMdmPostService mdmPostService;

    @Autowired
    private IMdmWeekService weekService;

    @Autowired
    private IMdmShiftGroupService mdmShiftGroupService;

    @Autowired
    private GetDeptChildUtil getDeptChildUtil;


    @Override
    public IPage<ManageCheckList> queryPageList(ManageCheckList manageCheckList, Integer pageNo, Integer pageSize) {
        List<ActionPlanProblemType> actionProblemTypeList = actionPlanProblemTypeService.list(new LambdaQueryWrapper<ActionPlanProblemType>().eq(ActionPlanProblemType::getProblemName, "分层检查"));
        if(CollUtil.isEmpty(actionProblemTypeList)){
            return null;
        }
        //判断是否需要查询部门下级，1为需要
        if(1 == manageCheckList.getQueryDeptChild()){
            manageCheckList.setCheckUserDept(getDeptChildUtil.getSqlStrByDeptId(manageCheckList.getCheckUserDept()));
            manageCheckList.setCheckOwnerDept(getDeptChildUtil.getSqlStrByDeptId(manageCheckList.getCheckOwnerDept()));
        } else {
            manageCheckList.setCheckUserDept(getDeptChildUtil.getConversionToSqlStr(manageCheckList.getCheckUserDept()));
            manageCheckList.setCheckOwnerDept(getDeptChildUtil.getConversionToSqlStr(manageCheckList.getCheckOwnerDept()));
        }
        manageCheckList.setCheckUser(getDeptChildUtil.getConversionToSqlStr(manageCheckList.getCheckUser()));
        manageCheckList.setCheckOwner(getDeptChildUtil.getConversionToSqlStr(manageCheckList.getCheckOwner()));
        manageCheckList.setCheckUserPost(getDeptChildUtil.getConversionToSqlStr(manageCheckList.getCheckUserPost()));
        manageCheckList.setCheckOwnerPost(getDeptChildUtil.getConversionToSqlStr(manageCheckList.getCheckOwnerPost()));
        if(StrUtil.isNotBlank(manageCheckList.getProblemType())){
            String[] problemTypeArr = manageCheckList.getProblemType().split(",");
            if(1 < problemTypeArr.length){
                manageCheckList.setProblemTypeArr(problemTypeArr);
                manageCheckList.setProblemType(null);
            }
        }
        Page<ManageCheckList> page = new Page<>(pageNo, pageSize);
        return baseMapper.queryPageList(page, manageCheckList, actionProblemTypeList.get(0).getId());
    }

    @Override
    @Transactional
    public Result<String> saveManageCheckList(ManageCheckListDto dto) {
        //拷贝数据设置id
        ManageCheckList manageCheck = new ManageCheckList();
        dto.setId(IdUtil.getSnowflakeNextIdStr());
        BeanUtil.copyProperties(dto, manageCheck);
        //保存行动计划数据
        addManageCheckActionPlan(dto);
        //保存管理者检查清单数据
        this.save(dto);
        return Result.OK(GlobalErrorEnum.SUCCESS.getCode(), dto.getId());
    }

    @Override
    @Transactional
    public Result<String> updateManageCheckList(ManageCheckListDto dto) {
        //保存行动计划数据
        addManageCheckActionPlan(dto);
        //修改管理者检查清单数据
        this.updateById(dto);
        return Result.OK(GlobalErrorEnum.SUCCESS.getCode(), dto.getId());
    }

    @Override
    @Transactional
    public void deleteBatch(List<ManageCheckList> deleteList) {
        //将需要删除的数据分组
        Map<String, List<ManageCheckList>> groupMap = deleteList.stream()
                .collect(Collectors.groupingBy(ManageCheckList::getId));

        //循环分层检查清单map
        for (Map.Entry<String, List<ManageCheckList>> entry : groupMap.entrySet()) {
            //取出行动计划id集合,批量删除
            List<String> actionIds = entry.getValue().stream()
                    .filter(manageCheck -> null != manageCheck.getActionPlanTrack())
                    .map(manageCheck -> manageCheck.getActionPlanTrack().getId().toString())
                    .collect(Collectors.toList());
            boolean isExistsAction = false;
            if(CollUtil.isNotEmpty(actionIds)){
                actionPlanTrackService.removeByIds(actionIds);
                //检查是否还有关联的行动计划数据，没有的话删除分层检查清单数据
                isExistsAction = actionPlanTrackService.exists(new LambdaQueryWrapper<ActionPlanTrack>().eq(ActionPlanTrack::getDataId, entry.getKey()));
            }
            if(!isExistsAction){
                this.removeById(entry.getKey());
            }
        }
    }

    @Override
    @Transactional
    public Result<?> importData(HttpServletRequest request, HttpServletResponse response, Class<ManageCheckList> clazz) throws IOException {
        //获取导入文件
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

        //错误信息集合
        List<String> errorMessage = new ArrayList<>();
        //成功数，错误数
        int successLines = 0, errorLines = 0;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(0);
            params.setHeadRows(0);
            params.setNeedSave(true);
            try {
                //获取上传文件中的数据
                List<ManageCheckList> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);
                //所有用户信息
                List<SysUser> allUserList = sysUserService.list();
                Map<String, SysUser> allUserMap = CollStreamUtil.toMap(allUserList, SysUser::getId, bean -> bean);
                //所有单位信息
                List<SysDepart> allDeptList = sysDepartService.list();
                Map<String, SysDepart> allDeptMap = CollStreamUtil.toMap(allDeptList, SysDepart::getId, bean -> bean);
                //获取问题类型数据
                List<ManageCheckCategory> problemTypeList = manageCheckCategoryService.list();
                Map<String, ManageCheckCategory> problemMap = CollStreamUtil.toMap(problemTypeList, ManageCheckCategory::getCode, bean -> bean);
                //获取检查数据记录格式字典
                List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
                        List.of(
                                "MES3_SUB_CLASS"
                                , "MES3_5S_POINT"
                                , "MES3_ACTION_PLAN_IMPORTANCE_EVALUATION"
                                , "MES3_ACTION_PLAN_LEVEL"
                        )
                );
                //字典数据map集合，key为groupCode-itemVal,value为对象信息
                Map<String, MdmDict> dictMap = mdmDictList.stream()
                        .collect(Collectors.toMap(
                                item -> item.getGroupCode() + "-" + item.getItemVal(),
                                item -> item
                        ));

                //获取岗位数据
                List<MdmPost> postList = mdmPostService.list();
                //岗位数据map集合，key为岗位id,value为对象信息
                Map<Integer, MdmPost> postMap = postList.stream()
                        .collect(Collectors.toMap(
                                MdmPost::getId,
                                item -> item
                        ));
                //岗位数据map集合，key为岗位名称,value为对象信息
                Map<String, MdmPost> postNameMap = postList.stream()
                        .collect(Collectors.toMap(
                                item -> item.getPostName()+"检查",
                                item -> item
                        ));

                //获取班组数据
                List<MdmShiftGroup> groupList = mdmShiftGroupService.list();
                //班组数据map集合，key为班组id,value为对象信息
                Map<String, MdmShiftGroup> groupMap = groupList.stream()
                        .collect(Collectors.toMap(
                                MdmShiftGroup::getId,
                                item -> item
                        ));

                //操作数据
                int lineNumber = 1;
                for(ManageCheckList manegeCheck : list){
                    if(null == manegeCheck.getCheckDate()){
                        errorMessage.add("第" + lineNumber + "行，日期不能为空");
                        errorLines++;
                        continue;
                    }
                    //获取所属周
                    List<MdmWeek> weekList = weekService.list(
                            new LambdaQueryWrapper<MdmWeek>()
                                    .le(MdmWeek::getStartDate, manegeCheck.getCheckDate())
                                    .ge(MdmWeek::getEndDate, manegeCheck.getCheckDate())
                    );
                    if(CollUtil.isNotEmpty(weekList)){
                        manegeCheck.setWeek(weekList.get(0).getWeekNumber());
                    }
                    if(null == manegeCheck.getProblemDesc() || manegeCheck.getProblemDesc().length() < 8){
                        errorMessage.add("第" + lineNumber + "行，问题描述不能少于8个字");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(manegeCheck.getCheckType()) || !postNameMap.containsKey(manegeCheck.getCheckType())){
                        errorMessage.add("第" + lineNumber + "行，检查类型不存在");
                        errorLines++;
                        continue;
                    }
                    manegeCheck.setCheckType(String.valueOf(postNameMap.get(manegeCheck.getCheckType()).getId()));

                    if(StrUtil.isBlank(manegeCheck.getCheckUser()) || !allUserMap.containsKey(manegeCheck.getCheckUser())){
                        errorMessage.add("第" + lineNumber + "行，检查人不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(manegeCheck.getCheckUserPost()) || !postMap.containsKey(Integer.valueOf(manegeCheck.getCheckUserPost()))){
                        errorMessage.add("第" + lineNumber + "行，检查人岗位不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(manegeCheck.getCheckUserDept()) || !allDeptMap.containsKey(manegeCheck.getCheckUserDept())){
                        errorMessage.add("第" + lineNumber + "行，检查部门不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(manegeCheck.getCheckOwner()) || !allUserMap.containsKey(manegeCheck.getCheckOwner())){
                        errorMessage.add("第" + lineNumber + "行，责任人不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(manegeCheck.getCheckOwnerPost()) || !postMap.containsKey(Integer.valueOf(manegeCheck.getCheckOwnerPost()))){
                        errorMessage.add("第" + lineNumber + "行，责任人岗位不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(manegeCheck.getCheckOwnerDept()) || !allDeptMap.containsKey(manegeCheck.getCheckOwnerDept())){
                        errorMessage.add("第" + lineNumber + "行，责任部门不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(manegeCheck.getCheckOwnerClassGroup()) || !groupMap.containsKey(manegeCheck.getCheckOwnerClassGroup())){
                        errorMessage.add("第" + lineNumber + "行，责任班组不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(manegeCheck.getCheckOwnerAdmin()) || !allUserMap.containsKey(manegeCheck.getCheckOwnerAdmin())){
                        errorMessage.add("第" + lineNumber + "行，责任管理人不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(manegeCheck.getProblemType()) || !problemMap.containsKey(manegeCheck.getProblemType())){
                        errorMessage.add("第" + lineNumber + "行，问题分类不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(manegeCheck.getProblemSubclass()) || !problemMap.containsKey("MES3_SUB_CLASS-"+manegeCheck.getProblemSubclass())){
                        errorMessage.add("第" + lineNumber + "行，问题子项不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isNotBlank(manegeCheck.getFivesCheck()) && !dictMap.containsKey("MES3_5S_POINT-"+manegeCheck.getProblemType())){
                        errorMessage.add("第" + lineNumber + "行，5S检查点不存在");
                        errorLines++;
                        continue;
                    }
                    successLines++;
                }
                this.saveOrUpdateBatch(list);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                errorLines++;
                errorMessage.add("发生异常：" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
    }

    @Override
    public Map<String, List<ManageCheckListSummaryDto>> querySummaryList(ManageCheckList manageCheckList) {
        //获取数据
        IPage<ManageCheckList> pageList = this.queryPageList(manageCheckList, 1, 10000);
        if(CollUtil.isEmpty(pageList.getRecords())){
            return null;
        }
        //获取原始数据
        List<ManageCheckList> list = pageList.getRecords();
        //所有用户信息
        List<SysUser> allUserList = sysUserService.list();
        Map<String, SysUser> allUserMap = CollStreamUtil.toMap(allUserList, SysUser::getId, bean -> bean);

        //获取岗位数据
        List<MdmPost> postList = mdmPostService.list();
        //岗位数据map集合，key为岗位id,value为对象信息
        Map<String, MdmPost> postMap = postList.stream()
                .collect(Collectors.toMap(
                        item -> item.getId().toString(),
                        item -> item
                ));

        //获取班组数据
        List<MdmShiftGroup> groupList = mdmShiftGroupService.list();
        //班组数据map集合，key为班组id,value为对象信息
        Map<String, MdmShiftGroup> shiftGroupMap = groupList.stream()
                .collect(Collectors.toMap(
                        MdmShiftGroup::getId,
                        item -> item
                ));

        //返回数据
        Map<String, List<ManageCheckListSummaryDto>> result = new HashMap<>();

        //分组数组，依次为：发现人；班组；责任岗位；责任人。
        String[] groupArray = {"checkUser", "checkOwnerClassGroup", "checkOwnerPost", "checkOwner"};
        for (String s : groupArray) {
            result.put(s, processData(allUserMap, postMap, shiftGroupMap, list, s));
        }
        return result;
    }

    @Override
    public JSONObject queryIndexSummary(WSActionStatisticsDto dto) {
        List<ActionPlanProblemType> actionProblemTypeList = actionPlanProblemTypeService.list(new LambdaQueryWrapper<ActionPlanProblemType>().eq(ActionPlanProblemType::getProblemName, "分层检查"));
        if(CollUtil.isEmpty(actionProblemTypeList)){
            return null;
        }

        List<ActionPlanTrack> list6 = actionPlanTrackService.list(
                new LambdaQueryWrapper<ActionPlanTrack>()
                        .eq(ActionPlanTrack::getProblemTypeId, 6)
                        .between(ActionPlanTrack::getPlanFinishDate, dto.getOpTime(), dto.getEdTime())
                        .eq(ActionPlanTrack::getStatus, 1)
        );

        //所有单位信息
        List<SysDepart> allDeptList = sysDepartService.list();
        Map<String, String> idDeptMap = allDeptList.stream()
                .filter(item -> "4".equals(item.getOrgType()))
                .collect(Collectors.toMap(SysDepart::getId, SysDepart::getDepartName));

        Map<String, Object> map2 = new HashMap<>(32);
        JSONObject hashMap = new JSONObject();
        if (CollUtil.isNotEmpty(list6)) {
            int size1 = list6.size();
            int count = (int) list6.stream().filter(actionTrack -> "1".equals(actionTrack.getFinishState()) || "4".equals(actionTrack.getFinishState())).count();
            if (0 == count) {
                map2.put("工厂整改行动计划完成率", 0);
            } else {
                map2.put("工厂整改行动计划完成率", NumberUtil.div(count, size1));
            }
            List<StatisticalVo> list10 = Lists.newArrayList();
            //责任人部门
            Map<String, List<ActionPlanTrack>> listMap = list6.stream()
                    .filter(x -> idDeptMap.containsKey(x.getDutyDeptId()))
                    .collect(Collectors.groupingBy(ActionPlanTrack::getDutyDeptId));
            for (Map.Entry<String, List<ActionPlanTrack>> entry : listMap.entrySet()) {
                String k = entry.getKey();
                List<ActionPlanTrack> v = entry.getValue();
                synchronizationCodeVo(list10, k, v, idDeptMap);
            }
            List<StatisticalVo> collect1 = list10.stream().sorted(Comparator.comparing(StatisticalVo::getLv).reversed()).collect(Collectors.toList());
            map2.put("分层检查完成率排名", collect1);
        } else {
            map2.put("工厂整改行动计划完成率", 0);
            map2.put("分层检查完成率排名", Lists.newArrayList());
        }
        hashMap.putOpt("分层检查", map2);

        return hashMap;
    }

    @Override
    public List<StatisticalVo> queryIndexSummaryList(WSActionStatisticsDto dto) {
        List<ActionPlanTrack> actionTracks = actionPlanTrackService.list(
                new LambdaQueryWrapper<ActionPlanTrack>()
                        .eq(ActionPlanTrack::getDutyDeptId, dto.getDeptId())
                        .eq(ActionPlanTrack::getProblemTypeId, 6)
                        .between(ActionPlanTrack::getPlanFinishDate, dto.getOpTime(), dto.getEdTime())
                        .eq(ActionPlanTrack::getStatus, 1)
        );
        List<StatisticalVo> list9 = Lists.newArrayList();
        int size = 0, yiWanCheng = 0, yanChi = 0, jinxingZ = 0;
        double yiWanChengLv = 0, yanChiLv = 0, jinxingZLv = 0;
        if (CollUtil.isNotEmpty(actionTracks)) {
            for (ActionPlanTrack a : actionTracks) {
                if (Objects.isNull(a.getFinishState())) continue;
                switch (a.getFinishState()) {
                    case "1" -> yiWanCheng++;
                    case "2" -> yanChi++;
                    case "0" -> jinxingZ++;
                }
                size++;
            }
            //已完成
            yiWanChengLv = (0 == yiWanCheng) ? 0 : NumberUtil.div(yiWanCheng, size);
            //延迟
            yanChiLv = (0 == yanChi) ? 0 : NumberUtil.div(yanChi, size);
            //进行中
            jinxingZLv = (0 == jinxingZ) ? 0 : NumberUtil.div(jinxingZ, size);

        }
        list9.add(new StatisticalVo("completed", yiWanCheng, yiWanChengLv, null));
        list9.add(new StatisticalVo("postpone", yanChi, yanChiLv, null));
        list9.add(new StatisticalVo("underWay", jinxingZ, jinxingZLv, null));
        return list9;
    }

    private void synchronizationCodeVo(List<StatisticalVo> list6, String k, List<ActionPlanTrack> v, Map<String, String> idDeptMap) {
        StatisticalVo vo = new StatisticalVo();
        String deptName = idDeptMap.get(k);
        vo.setName(deptName);
        int size1 = v.size();
        vo.setSize(size1);
        int count1 = (int) v.stream().filter(track -> "1".equals(track.getFinishState()) || "4".equals(track.getFinishState())).count();
        if (0 == count1) {
            vo.setLv(0);
        } else {
            vo.setLv(NumberUtil.div(count1, size1));
        }
        list6.add(vo);
    }

    /**
     * 添加管理者检查清单关联的行动计划
     */
    private void addManageCheckActionPlan(ManageCheckListDto dto){
        if(CollUtil.isNotEmpty(dto.getActionPlanTrackList())){
            //查询已存在的行动计划,且有效的行动计划
            List<ActionPlanTrack> oldActionList = actionPlanTrackService.list(
                    new LambdaQueryWrapper<ActionPlanTrack>()
                            .eq(ActionPlanTrack::getDataId, dto.getId())
            );
            //将数据转换成map集合,方便后续判断是否存在
            Map<Integer, ActionPlanTrack> actionMap = oldActionList.stream()
                    .collect(Collectors.toMap(ActionPlanTrack::getId, actionPlanTrack -> actionPlanTrack));

            //需要进行操作的行动计划集合
            List<ActionPlanTrack> optList = new ArrayList<>();
            dto.getActionPlanTrackList().forEach(actionPlan -> {
                actionPlan.setDataId(dto.getId());
                if(null == actionPlan.getId()){
                    //如果id为空直接添加
                    optList.add(actionPlan);
                }else if(actionMap.containsKey(actionPlan.getId())){
                    //如果已存在行动计划数据
                    ActionPlanTrack oldAction = actionMap.get(actionPlan.getId());
                    //无效状态可继续修改
                    if("0".equals(oldAction.getStatus())){
                        optList.add(actionPlan);
                    } else {
                        //有效状态只修改图片
                        oldAction.setPathText(dto.getProblemImg());
                        oldAction.setFileObj(dto.getOptimizeImg());
                        optList.add(oldAction);
                    }
                }
            });
            if(CollUtil.isNotEmpty(optList)){
                actionPlanTrackService.saveOrUpdateBatch(optList);
            }
        }
    }

    /**
     * 根据指定字段进行分组
     */
    private <T> Object getValueByFieldName(T obj, String fieldName) {
        if(StrUtil.isBlank(fieldName)){
            return null;
        }
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {

            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     *
     * 处理计算
     * @param allUserMap 用户鼩
     * @param postMap 岗位数据
     * @param shiftGroupMap 班组数据
     * @param list 原始数据
     * @param fieldName 需要分组的字段名称
     */
    private List<ManageCheckListSummaryDto> processData(Map<String, SysUser> allUserMap, Map<String, MdmPost> postMap, Map<String, MdmShiftGroup> shiftGroupMap, List<ManageCheckList> list, String fieldName){
        List<ManageCheckListSummaryDto> reslut = new ArrayList<>(0);
        //根据指定字段将原始数据分组
        Map<String, List<ManageCheckList>> groupMap = list.stream()
                .filter(o -> {
                    Object fieldValue = getValueByFieldName(o, fieldName);
                    // 过滤掉字段值为null或空字符串的情况
                    return fieldValue != null && !fieldValue.toString().trim().isEmpty();
                })
                .collect(Collectors.groupingBy(o -> Objects.requireNonNull(getValueByFieldName(o, fieldName)).toString()));

        //问题数量汇总
        int sumProblemCount = 0;
        //5s管理数量汇总
        int sumFivesCount = 0;
        //5s管理完成数汇总
        int sumFivesFinishCount = 0;
        //非5s管理完成数汇总
        int sumNoFivesFinishCount = 0;
        for (Map.Entry<String, List<ManageCheckList>> entry : groupMap.entrySet()) {
            String key = entry.getKey();
            List<ManageCheckList> manageCheckList = entry.getValue();
            //问题数量
            int problemCount = 0;
            //5s管理数量
            int fivesCount = 0;
            //5s管理完成数
            int fivesFinishCount = 0;
            //非5s管理完成数
            int noFivesFinishCount = 0;
            //因为是关联查询行动计划，所以需要根据id再次分组
            Map<String, List<ManageCheckList>> groupByIdMap = manageCheckList.stream()
                    .collect(Collectors.groupingBy(ManageCheckList::getRowId));
            for (Map.Entry<String, List<ManageCheckList>> childEntry : groupByIdMap.entrySet()) {
                problemCount++;
                //107开头为5s管理分类
                if(childEntry.getValue().get(0).getProblemType().startsWith("107")){
                    fivesCount++;
                }

                //子项总数
                int size = childEntry.getValue().size();
                //循环子项获取完成数据
                int tempFivesCount = 0;
                int tempNoFivesCount = 0;
                for(ManageCheckList m : childEntry.getValue()){
                    if(null != m.getActionPlanTrack()){
                        if("1".equals(m.getActionPlanTrack().getFinishState()) || "4".equals(m.getActionPlanTrack().getFinishState())){
                            if(childEntry.getValue().get(0).getProblemType().startsWith("107")){
                                tempFivesCount++;
                            } else {
                                tempNoFivesCount++;
                            }
                        }
                    }
                }
                //如果子项总数等于子项的完成数，则对应的父项完成数+1
                if(size == tempFivesCount){
                    fivesFinishCount++;
                } else if (size == tempNoFivesCount){
                    noFivesFinishCount++;
                }
            }

            String dynamicText = key;
            dynamicText = switch (fieldName) {
                case "checkUser", "checkOwner" -> null != allUserMap.get(key) ? allUserMap.get(key).getRealname() : "" ;
                case "checkOwnerClassGroup" ->
                        shiftGroupMap.containsKey(key) ? shiftGroupMap.get(key).getGroupName() : key;
                case "checkOwnerPost" -> postMap.containsKey(key) ? postMap.get(key).getPostName() : key;
                default -> dynamicText;
            };

            //累加汇总数据
            sumProblemCount += problemCount;
            sumFivesCount += fivesCount;
            sumFivesFinishCount += fivesFinishCount;
            sumNoFivesFinishCount += noFivesFinishCount;
            //计算数据
            reslut.add(calcData(dynamicText, problemCount, fivesCount, fivesFinishCount, noFivesFinishCount));
        }
        //计算汇总数据
        reslut.add(calcData("汇总", sumProblemCount, sumFivesCount, sumFivesFinishCount, sumNoFivesFinishCount));
        return reslut;
    }

    /**
     * 计算数据
     * @param dynamicText 显示文字
     * @param problemCount 问题总数
     * @param fivesCount 5s管理数
     * @param fivesFinishCount 5s管理完成数
     * @param noFivesFinishCount 非5s管理完成数
     */
    private ManageCheckListSummaryDto calcData(String dynamicText, int problemCount, int fivesCount, int fivesFinishCount, int noFivesFinishCount){
        //计算非5s管理数量
        int noFivesCount = problemCount - fivesCount;
        //计算完成数
        int finishCount = fivesFinishCount + noFivesFinishCount;
        ManageCheckListSummaryDto dto = new ManageCheckListSummaryDto();
        dto.setDynamicText(dynamicText);
        dto.setProblemCount(problemCount);
        dto.setFivesCount(fivesCount);
        dto.setNoFivesCount(noFivesCount);
        dto.setFivesFinishCount(fivesFinishCount);
        dto.setNoFivesFinishCount(noFivesFinishCount);
        dto.setFivesFinishRate(calcRate(fivesFinishCount, fivesCount));
        dto.setNoFivesFinishRate(calcRate(noFivesFinishCount, noFivesCount));
        dto.setFinishCount(finishCount);
        dto.setFinishRate(calcRate(finishCount, problemCount));
        return dto;
    }

    /**
     * 计算百分比
     * @param part 分子
     * @param whole 分母
     */
    private String calcRate(int part, int whole){
        if (whole == 0) {
            return "0.00"; // 处理除以零的情况
        }

        double percentage = ((double) part / whole) * 100;
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(percentage);
    }
}
