package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.kpi.entity.KpiPlatoOne;
import com.keytech.modules.kpi.entity.KpiPlatoThree;
import com.keytech.modules.kpi.entity.KpiPlatoTwo;
import com.keytech.modules.kpi.mapper.KpiPlatoThreeMapper;
import com.keytech.modules.kpi.service.IKpiPlatoOneService;
import com.keytech.modules.kpi.service.IKpiPlatoThreeService;
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
 * @Description: 三级柏拉图(PI点)
 * @Author: jeecg-boot
 * @Date:   2025-02-18
 * @Version: V1.0
 */
@Service
public class KpiPlatoThreeServiceImpl extends BaseServiceImpl<KpiPlatoThreeMapper, KpiPlatoThree> implements IKpiPlatoThreeService {

    @Autowired
    private IKpiPlatoTwoService kpiPlatoTwoService;

    /**
     * 新增
     * @param kpiPlatoThree
     */
    @Override
    public void addThree(KpiPlatoThree kpiPlatoThree) {
        String parentCode = kpiPlatoThree.getParentCode();
        LambdaQueryWrapper<KpiPlatoThree> queryWrapper = Wrappers.lambdaQuery(KpiPlatoThree.class);
        queryWrapper.select(KpiPlatoThree::getId, KpiPlatoThree::getCode, KpiPlatoThree::getParentCode);
        queryWrapper.eq(KpiPlatoThree::getFactoryCode, kpiPlatoThree.getFactoryCode());
        queryWrapper.eq(StrUtil.isNotBlank(kpiPlatoThree.getDeptCode()),KpiPlatoThree::getDeptCode, kpiPlatoThree.getDeptCode());
        queryWrapper.eq(KpiPlatoThree::getParentCode, parentCode);
        queryWrapper.orderByDesc(KpiPlatoThree::getCode);
        queryWrapper.last("limit 1");
        KpiPlatoThree platoThree = getOne(queryWrapper);
        String code;
        if (platoThree == null) {
            code = YouBianCodeUtil.getSubKpiCode(parentCode, null);
        } else {
            String subCode = platoThree.getCode();
            code = YouBianCodeUtil.getSubKpiCode(parentCode, subCode);
        }
        kpiPlatoThree.setCode(code);
        save(kpiPlatoThree);
    }

    /**
     * 导入
     * @param request
     * @param response
     * @param kpiPlatoThreeClass
     * @return
     */
    @Override
    public Result<?> platoThreeImportExcel(HttpServletRequest request, HttpServletResponse response, Class<KpiPlatoThree> kpiPlatoThreeClass) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String factory = sysUser.getFactoryCode();
        //二级柏拉图
        List<KpiPlatoTwo> mdmKpiPlatoTwoList = kpiPlatoTwoService.list(new LambdaQueryWrapper<KpiPlatoTwo>().likeRight(KpiPlatoTwo::getFactoryCode, factory));
        //Map<String, KpiPlatoTwo> mdmKpiPlatoTwoMap = CollStreamUtil.toMap(mdmKpiPlatoTwoList, KpiPlatoTwo::getCode, bean -> bean);
        //Map<String, String> piMapping = mapMdmPiMappingService.getPIMapping(factory, null, null);
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
//                public void setNeedVerifyFields(String[] arr) {
//
//                }
//
//                @Override
//                public ExcelVerifyHanlderResult verifyHandler(Object obj, String name, Object value) {
//                    ExcelVerifyHanlderResult result = new ExcelVerifyHanlderResult();
//                    if (name.equals("控制点名称")) {
//                        result.setSuccess(mdmKpiPlatoTwoMap.containsKey(value + ""));
//                    }
//                    return result;
//                }
//            });
            //获取 年_kpiName map
            Map<String, KpiPlatoTwo> platoTwoMap = CollStreamUtil.toMap(mdmKpiPlatoTwoList, obj -> obj.getControllerName(), bean -> bean);
            //获取 code name map
            Map<String, String> platoTwoNameMap = CollStreamUtil.toMap(mdmKpiPlatoTwoList, KpiPlatoTwo::getCode, bean -> bean.getControllerName());

            try {
                List<KpiPlatoThree> list = ExcelImportUtil.importExcel(file.getInputStream(), KpiPlatoThree.class, params);
                long start = System.currentTimeMillis();
                for (KpiPlatoThree mdmKpiPlatoThree : list) {
                    //设置superCode
                    //代码得到名称
                    String name = platoTwoNameMap.get(mdmKpiPlatoThree.getParentCode());
                    //匹配年对应的二级柏拉图
                    KpiPlatoTwo kpiPlatoTwo = platoTwoMap.get(mdmKpiPlatoThree.getYear()+"_"+name);
                    if(kpiPlatoTwo != null){
                        mdmKpiPlatoThree.setParentCode(kpiPlatoTwo.getCode());
                        mdmKpiPlatoThree.setSuperCode(kpiPlatoTwo.getSuperCode());
                    }
                    addThree(mdmKpiPlatoThree);
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
