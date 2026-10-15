package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiPlatoOne;
import com.keytech.modules.kpi.entity.KpiPlatoTwo;
import com.keytech.modules.kpi.mapper.KpiPlatoTwoMapper;
import com.keytech.modules.kpi.service.IKpiPlatoOneService;
import com.keytech.modules.kpi.service.IKpiPlatoTwoService;
import com.keytech.modules.kpi.util.YouBianCodeUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.result.ExcelVerifyHanlderResult;
import org.jeecgframework.poi.handler.inter.IExcelVerifyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description: 二级柏拉图（控制点）
 * @Author: jeecg-boot
 * @Date:   2025-02-10
 * @Version: V1.0
 */
@Service
public class KpiPlatoTwoServiceImpl extends BaseServiceImpl<KpiPlatoTwoMapper, KpiPlatoTwo> implements IKpiPlatoTwoService {

    @Autowired
    private IKpiPlatoOneService kpiPlatoOneService;

    /**
     * 二级柏拉图 保存
     * @param kpiPlatoTwo
     */
    @Override
    public void platoTwoSave(KpiPlatoTwo kpiPlatoTwo) {
        String parentCode = kpiPlatoTwo.getParentCode();
        LambdaQueryWrapper<KpiPlatoTwo> queryWrapper = Wrappers.lambdaQuery(KpiPlatoTwo.class);
        queryWrapper.select(KpiPlatoTwo::getId, KpiPlatoTwo::getCode, KpiPlatoTwo::getParentCode);
        queryWrapper.eq(KpiPlatoTwo::getFactoryCode, kpiPlatoTwo.getFactoryCode());
        queryWrapper.eq(StrUtil.isNotBlank(kpiPlatoTwo.getDeptCode()),KpiPlatoTwo::getDeptCode, kpiPlatoTwo.getDeptCode());
        queryWrapper.eq(KpiPlatoTwo::getParentCode, parentCode);
        queryWrapper.eq(KpiPlatoTwo::getYear, kpiPlatoTwo.getYear());
        queryWrapper.orderByDesc(KpiPlatoTwo::getCode);
        queryWrapper.last("limit 1");
        KpiPlatoTwo platoThree = getOne(queryWrapper);
        String code;
        if (platoThree == null) {
            code = YouBianCodeUtil.getSubKpiCode(parentCode, null);
        } else {
            String subCode = platoThree.getCode();
            code = YouBianCodeUtil.getSubKpiCode(parentCode, subCode);
        }
        kpiPlatoTwo.setCode(code);
        save(kpiPlatoTwo);
    }


    /**
     * 二级柏拉图 导入
     * @param request
     * @param response
     * @param kpiPlatoTwoClass
     * @return
     */
    @Override
    public Result<?> platoTwoImportExcel(HttpServletRequest request, HttpServletResponse response, Class<KpiPlatoTwo> kpiPlatoTwoClass) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        //一级柏拉图
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String factory = sysUser.getFactoryCode();
        List<KpiPlatoOne> kpiPlatoOneList = kpiPlatoOneService.list(new LambdaQueryWrapper<KpiPlatoOne>().likeRight(KpiPlatoOne::getFactoryCode, factory));
        //Map<String, KpiPlatoOne> mdmKpiPlatoOneMap = CollStreamUtil.toMap(kpiPlatoOneList, KpiPlatoOne::getCode, bean -> bean);
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
//            params.setVerifyHanlder(new IExcelVerifyHandler() {
//                @Override
//                public String[] getNeedVerifyFields() {
//                    return new String[]{"parentCode"};
//                }
//
//                @Override
//                public void setNeedVerifyFields(String[] arr) {}
//                @Override
//                public ExcelVerifyHanlderResult verifyHandler(Object obj, String name, Object value) {
//                    ExcelVerifyHanlderResult result = new ExcelVerifyHanlderResult();
//                    result.setSuccess(mdmKpiPlatoOneMap.containsKey(value + ""));
//                    return result;
//                }
//            });

            //获取 年_Name map
            Map<String, KpiPlatoOne> platoOneMap = CollStreamUtil.toMap(kpiPlatoOneList, obj -> obj.getProcessName(), bean -> bean);
            //获取 code name map
            Map<String, String> platoOneNameMap = CollStreamUtil.toMap(kpiPlatoOneList, KpiPlatoOne::getCode, bean -> bean.getProcessName());
            try {
                List<KpiPlatoTwo> list = ExcelImportUtil.importExcel(file.getInputStream(), KpiPlatoTwo.class, params);
                for (KpiPlatoTwo kpiPlatoTwo : list) {
                    //设置superCode(一级柏拉图)
                    //代码得到名称
                    String name = platoOneNameMap.get(kpiPlatoTwo.getParentCode());
                    //匹配年对应的一级柏拉图
                    KpiPlatoOne kpiPlatoOne = platoOneMap.get(name);
                    if (kpiPlatoOne != null) {
                        kpiPlatoTwo.setParentCode(kpiPlatoOne.getCode());
                        kpiPlatoTwo.setSuperCode(kpiPlatoOne.getSuperCode());
                    }
                    platoTwoSave(kpiPlatoTwo);
                }
                return Result.ok("文件导入成功！数据行数：" + list.size());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("文件导入失败:" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.error("文件导入失败！");
    }
}
