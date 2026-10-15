package com.keytech.modules.practice.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.entity.MdmShiftGroup;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.mdm3.service.IMdmShiftGroupService;
import com.keytech.modules.practice.entity.BestPracticeFill;
import com.keytech.modules.practice.entity.BestPracticeType;
import com.keytech.modules.practice.mapper.BestPracticeFillMapper;
import com.keytech.modules.practice.service.IBestPracticeFillService;
import com.keytech.modules.practice.service.IBestPracticeTypeService;
import groovy.util.logging.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 最佳实践-填报
 * @Author: jeecg-boot
 * @Date:   2025-03-17
 * @Version: V1.0
 */
@Slf4j
@Service
public class BestPracticeFillServiceImpl extends BaseServiceImpl<BestPracticeFillMapper, BestPracticeFill> implements IBestPracticeFillService {
    @Autowired
    private IMdmDictService mdmDictService;
    @Autowired
    private IMdmShiftGroupService mdmShiftGroupService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private IBestPracticeTypeService bestPracticeTypeService;
    @Override
    @Transactional
    public Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
            params.setTitleRows(2);
            params.setHeadRows(3);
            params.setNeedSave(true);
            try {
                //获取所有用户信息，并按id做key转换成map
                List<SysUser> allUserList = sysUserService.list();
                Map<String, SysUser> userMap = CollStreamUtil.toMap(allUserList, SysUser::getId, bean -> bean);
                //获取改善类型数据
                List<BestPracticeType> typeList = bestPracticeTypeService.list();
                Map<String, BestPracticeType> typeMap = CollStreamUtil.toMap(typeList, BestPracticeType::getId, bean -> bean);
                //获取所有单位信息，并按id做key转换成map
                List<SysDepart> allDeptList = sysDepartService.list();
                Map<String, SysDepart> deptMap = CollStreamUtil.toMap(allDeptList, SysDepart::getOrgCode, bean -> bean);
                //提报班组
                List<MdmShiftGroup> shiftGroupList = mdmShiftGroupService.list();
                Map<String, MdmShiftGroup> shiftGroupMap = CollStreamUtil.toMap(shiftGroupList, MdmShiftGroup::getId, bean -> bean);
                //获取当前用户信息
                LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

                //获取字典
                List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
                        List.of(
                                "MES3_BEST_EFFECT"
                        )
                );
                Map<String, MdmDict> dictMap = mdmDictList.stream()
                        .collect(Collectors.toMap(
                                item -> item.getGroupCode() + "-" + item.getItemVal(), // 键是由groupCode和val组成的字符串
                                item -> item // 值是对象本身
                        ));

                //获取上传文件中的数据
                List<BestPracticeFill> list = ExcelImportUtil.importExcel(file.getInputStream(), BestPracticeFill.class, params);
                int lineNumber = 3;
                for(BestPracticeFill bestPracticeFill : list){
                    lineNumber++;
                    if(StrUtil.isBlank(bestPracticeFill.getTitle())){
                        errorMessage.add("第" + lineNumber + "行，名称不能为空");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(bestPracticeFill.getUserId())
                            || !userMap.containsKey(bestPracticeFill.getUserId())){
                        errorMessage.add("第" + lineNumber + "行，提报员工不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isNotBlank(bestPracticeFill.getShiftGroupId())
                            && !shiftGroupMap.containsKey(bestPracticeFill.getShiftGroupId())){
                        errorMessage.add("第" + lineNumber + "行，提报班组不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(bestPracticeFill.getReportDeptId())
                            || !deptMap.containsKey(bestPracticeFill.getReportDeptId())){
                        errorMessage.add("第" + lineNumber + "行，提报部门不存在");
                        errorLines++;
                        continue;
                    }
                    bestPracticeFill.setReportDeptName(deptMap.get(bestPracticeFill.getReportDeptId()).getDepartName());
                    bestPracticeFill.setFactoryCode(deptMap.get(bestPracticeFill.getReportDeptId()).getOrgCode().substring(0, 9));

                    if(null == bestPracticeFill.getReportTime()){
                        errorMessage.add("第" + lineNumber + "行，提报时间不能为空");
                        errorLines++;
                        continue;
                    }
                    bestPracticeFill.setYearMon(DateUtil.format(bestPracticeFill.getReportTime(), "yyyy-MM"));
                    if(StrUtil.isBlank(bestPracticeFill.getId())){
                        //改善类型设计其他操作，因此导入时不修改改善类型
                        bestPracticeFill.setImprovementType(null);
                    } else {
                        if(StrUtil.isBlank(bestPracticeFill.getImprovementType())
                                || !typeMap.containsKey(bestPracticeFill.getImprovementType())){
                            errorMessage.add("第" + lineNumber + "行，改善类型不存在");
                            errorLines++;
                            continue;
                        }
                    }
                    if(StrUtil.isNotBlank(bestPracticeFill.getImprovementEffect()) &&
                            !dictMap.containsKey("MES3_BEST_EFFECT"+"-"+bestPracticeFill.getImprovementEffect())){
                        errorMessage.add("第" + lineNumber + "行，改善效果不存在");
                        errorLines++;
                        continue;
                    }
                    successLines++;
                }
                if(errorLines == 0){
                    this.saveOrUpdateBatch(list);
                }
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
}
