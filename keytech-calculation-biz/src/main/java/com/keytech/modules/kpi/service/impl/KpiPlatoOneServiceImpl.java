package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiPlatoOne;
import com.keytech.modules.kpi.mapper.KpiPlatoOneMapper;
import com.keytech.modules.kpi.service.IKpiInfoService;
import com.keytech.modules.kpi.service.IKpiPlatoOneService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 一级柏拉图（生产流程）
 * @Author: jeecg-boot
 * @Date:   2025-02-10
 * @Version: V1.0
 */
@Service
public class KpiPlatoOneServiceImpl extends BaseServiceImpl<KpiPlatoOneMapper, KpiPlatoOne> implements IKpiPlatoOneService {
    @Autowired
    private IKpiInfoService kpiInfoService;
    @Autowired
    private KpiPlatoOneMapper kpiPlatoOneMapper;
    /**
     * 一级柏拉图保存
     * @param kpiPlatoOne
     */
    @Override
    public void platoOneSave(KpiPlatoOne kpiPlatoOne) {
        String parentCode = kpiPlatoOne.getParentCode();
        LambdaQueryWrapper<KpiPlatoOne> queryWrapper = Wrappers.lambdaQuery(KpiPlatoOne.class);
        queryWrapper.select(KpiPlatoOne::getId, KpiPlatoOne::getCode, KpiPlatoOne::getParentCode);
        queryWrapper.eq(KpiPlatoOne::getFactoryCode, kpiPlatoOne.getFactoryCode());
        queryWrapper.eq(StrUtil.isNotBlank(kpiPlatoOne.getDeptCode()), KpiPlatoOne::getDeptCode,kpiPlatoOne.getDeptCode());
        queryWrapper.eq(KpiPlatoOne::getParentCode, parentCode);
        queryWrapper.orderByDesc(KpiPlatoOne::getCode);
        queryWrapper.last("limit 1");
        KpiPlatoOne platoOne = getOne(queryWrapper);
        String code;
        if (platoOne == null) {
            code = YouBianCodeUtil.getSubKpiCode(parentCode, null);
        } else {
            String subCode = platoOne.getCode();
            code = YouBianCodeUtil.getSubKpiCode(parentCode, subCode);
        }
        kpiPlatoOne.setCode(code);
        kpiPlatoOne.setSuperCode(parentCode);
        save(kpiPlatoOne);
    }


    /**
     * 一级柏拉图 导入
     * @param request
     * @param response
     * @param kpiPlatoOneClass
     * @return
     */
    @Override
    public Result<?> platoOneImportExcel(HttpServletRequest request, HttpServletResponse response, Class<KpiPlatoOne> kpiPlatoOneClass) {

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        String factory = request.getParameter("factory");
        //获取当前登陆人
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(null != sysUser){
            factory =  sysUser.getFactoryCode();
        }
        //查询KPI
        List<KpiInfo> kpiMapping = kpiInfoService.list(new LambdaQueryWrapper<KpiInfo>()
                .likeRight(KpiInfo::getFactoryCode, factory).eq(KpiInfo::getPlato, 1));
        Map<String, KpiInfo> kpiInfoMap = CollStreamUtil.toMap(kpiMapping, KpiInfo::getKpiName, bean -> bean);

        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile file = entity.getValue();// 获取上传文件对象
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
//                }
//
//                @Override
//                public ExcelVerifyHanlderResult verifyHandler(Object obj, String name, Object value) {
//                    ExcelVerifyHanlderResult result = new ExcelVerifyHanlderResult();
//                    result.setSuccess(kpiInfoMap.containsKey(value.toString()));
//                    result.setMsg("KPI不存在");
//                    return result;
//                }
//            });
            //获取 年_kpiName map
            Map<String, KpiInfo> kpiInfoYearMap = CollStreamUtil.toMap(kpiMapping,kpiInfo -> kpiInfo.getKpiName(), bean -> bean);
            //获取 code name map
            Map<String, String> kpiInfoNameMap = CollStreamUtil.toMap(kpiMapping, KpiInfo::getKpiCode, bean -> bean.getKpiName());
            try {
                List<KpiPlatoOne> list = ExcelImportUtil.importExcel(file.getInputStream(), KpiPlatoOne.class, params);
                for (KpiPlatoOne kpiPlatoOne : list) {
                    String key = kpiPlatoOne.getParentCode();
//                    String year = kpiPlatoOne.getYear();
                    String name = kpiInfoNameMap.get(key);
                    KpiInfo kpiInfo = kpiInfoYearMap.get(name);
                    if (kpiInfo != null) {
                        kpiPlatoOne.setParentCode(kpiInfo.getKpiCode());
                    }
                }
                list.forEach(bean -> platoOneSave(bean));
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


    /**
     * 获取柏拉图树
     * @param kpiCode
     * @return
     */
    @Override
    public Map<String, String> listMapByKpiCode(String kpiCode,String year) {
        List<KpiPlatoOne> allPlato = kpiPlatoOneMapper.listUnion(kpiCode, year);
        if (CollectionUtils.isEmpty(allPlato)){
            return null;
        }
        return allPlato.stream().collect(Collectors.toMap(KpiPlatoOne::getCode,KpiPlatoOne::getProcessName));
    }
}
