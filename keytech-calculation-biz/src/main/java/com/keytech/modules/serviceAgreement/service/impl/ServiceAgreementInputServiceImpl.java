package com.keytech.modules.serviceAgreement.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.serviceAgreement.dto.AgreementExceptionDto;
import com.keytech.modules.serviceAgreement.entity.ServiceAgreementInput;
import com.keytech.modules.serviceAgreement.mapper.ServiceAgreementInputMapper;
import com.keytech.modules.serviceAgreement.service.IServiceAgreementExceptionService;
import com.keytech.modules.serviceAgreement.service.IServiceAgreementInputService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 服务协议登记表
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Service
public class ServiceAgreementInputServiceImpl extends BaseServiceImpl<ServiceAgreementInputMapper, ServiceAgreementInput> implements IServiceAgreementInputService {

    @Autowired
    private IServiceAgreementExceptionService serviceAgreementExceptionService;

    @Autowired
    private ISysDepartService sysDepartService;

    @Override
    public List<ServiceAgreementInput> trackingList( List<ServiceAgreementInput> list, Integer year) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        //获取异常统计
        Map<String, ServiceAgreementInput> agreementInputMap = CollStreamUtil.toMap(list, ServiceAgreementInput::getId, bean -> bean);
        //默认当前年的数据统计
        List<AgreementExceptionDto> listExceptionList = serviceAgreementExceptionService.listSt(agreementInputMap.keySet(), null == year ? DateUtil.year(new Date()) : year);
        Map<String, AgreementExceptionDto> exceptionStMap = CollStreamUtil.toMap(listExceptionList
                , AgreementExceptionDto::getServiceAgreementId, bean -> bean);
        List<ServiceAgreementInput> result = new ArrayList<>();
        List<SysDepart> sysDepartList = sysDepartService.list();
        //sysDepartList转换为map其中orgcode为key
        Map<String, SysDepart> sysDepartMap = CollStreamUtil.toMap(sysDepartList, SysDepart::getOrgCode, bean -> bean);


        agreementInputMap.forEach((k, v) -> {
            SysDepart provideDept = sysDepartMap.get(v.getProvideDept());
            if(null != provideDept) {
                v.setProvideDept_text(provideDept.getDepartName());
            }
            SysDepart recDept = sysDepartMap.get(v.getReceiveDept());
            if(null != recDept) {
                v.setReceiveDept_text(recDept.getDepartName());
            }
            //目标值，根据用户输入目标值构造目标值本年内数据
            String monthTargetValue = v.getTargetValue();
            int targetValue = 0;
            if(StringUtils.isNotBlank(monthTargetValue)){
                targetValue = NumberUtil.parseInt(monthTargetValue);
            }
            //根据启动、停用时间判断目标值
            if(null == v.getEndDate()){
                v.setJan(targetValue);
                v.setApr(targetValue);
                v.setFeb(targetValue);
                v.setDec(targetValue);
                v.setAug(targetValue);
                v.setJul(targetValue);
                v.setMar(targetValue);
                v.setMay(targetValue);
                v.setNov(targetValue);
                v.setOct(targetValue);
                v.setSep(targetValue);
                v.setJun(targetValue);

            } else if(v.getStartDate().equals(v.getEndDate())){
                int month = DateUtils.getMonthByDate(v.getStartDate());
                if(month == 1){
                    v.setJan(targetValue);
                } else if(month == 2){
                    v.setFeb(targetValue);
                } else if(month == 3){
                    v.setMar(targetValue);
                }  else if(month == 4){
                    v.setApr(targetValue);
                } else if(month == 5){
                    v.setJun(targetValue);
                } else if(month == 6){
                    v.setJul(targetValue);
                } else if(month == 7){
                    v.setJan(targetValue);
                } else if(month == 8){
                    v.setAug(targetValue);
                } else if(month == 9){
                    v.setSep(targetValue);
                } else if(month == 10){
                    v.setOct(targetValue);
                } else if(month == 11){
                    v.setNov(targetValue);
                } else if(month == 12){
                    v.setDec(targetValue);
                }
            } else {
                List<Integer> month = DateUtils.getWeekNumberByDate(v.getStartDate(),v.getEndDate());
                for (Integer monthValue : month) {
                    String mon = DateUtils.getMonthMap(monthValue);
                    if (mon.equals("Jan")) {
                        v.setJan(targetValue);
                    } else if (mon.equals("Feb")) {
                        v.setFeb(targetValue);
                    } else if (mon.equals("Mar")) {
                        v.setMar(targetValue);
                    } else if (mon.equals("Apr")) {
                        v.setApr(targetValue);
                    } else if (mon.equals("May")) {
                        v.setMay(targetValue);
                    } else if (mon.equals("Jun")) {
                        v.setJun(targetValue);
                    } else if (mon.equals("Jul")) {
                        v.setJul(targetValue);
                    } else if (mon.equals("Aug")) {
                        v.setAug(targetValue);
                    } else if (mon.equals("Sep")) {
                        v.setSep(targetValue);
                    } else if (mon.equals("Oct")) {
                        v.setOct(targetValue);
                    } else if (mon.equals("Nov")) {
                        v.setNov(targetValue);
                    } else if (mon.equals("Dec")) {
                        v.setDec(targetValue);
                    }
                }
            }
            v.setTitle("目标值");
            result.add(v);
            //实际值，异常条数构造实际值本年内数据
            AgreementExceptionDto dto = exceptionStMap.getOrDefault(k, new AgreementExceptionDto());
            ServiceAgreementInput input = this.getById(dto.getServiceAgreementId());
            if(null == input){
                input = new ServiceAgreementInput();
                input.setId(v.getId());
                input.setReceiveDept(v.getReceiveDept());
                input.setProvideDept_text(v.getProvideDept_text());
                input.setReceiveDept_text(v.getReceiveDept_text());
                input.setProvideDept(v.getProvideDept());
                input.setActivity(v.getActivity());
                input.setProduct(v.getProduct());
                input.setServiceIndicator(v.getServiceIndicator());
                input.setUnit(v.getUnit());
            } else {
                SysDepart provide = sysDepartMap.get(v.getProvideDept());
                if(null != provide) {
                    input.setProvideDept_text(provide.getDepartName());
                }
                SysDepart rec = sysDepartMap.get(v.getReceiveDept());
                if(null != rec) {
                    input.setReceiveDept_text(rec.getDepartName());
                }
            }
            input.setJan(dto.getJan());
            input.setFeb(dto.getFeb());
            input.setMar(dto.getMar());
            input.setApr(dto.getApr());
            input.setMay(dto.getMay());
            input.setJun(dto.getJun());
            input.setJul(dto.getJul());
            input.setAug(dto.getAug());
            input.setSep(dto.getSep());
            input.setOct(dto.getOct());
            input.setNov(dto.getNov());
            input.setDec(dto.getDec());
            input.setYear(dto.getYear());
            input.setTitle("实际值");

            result.add(input);
        });
        return result;
    }



    @Override
    public Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<ServiceAgreementInput> clazz) throws IOException {
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
            params.setHeadRows(2);
            params.setNeedSave(true);
            try {
                //获取上传文件中的数据
                List<ServiceAgreementInput> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);
                //操作数据
                int lineNumber = 2;
                for(ServiceAgreementInput template : list){
                    lineNumber++;
                    if(StrUtil.isBlank(template.getProvideDept())){
                        errorMessage.add("第" + lineNumber + "行，提供方部门为空或不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(template.getProduct())){
                        errorMessage.add("第" + lineNumber + "行，产品/服务为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getActivity())){
                        errorMessage.add("第" + lineNumber + "行，活动为空或不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(template.getReceiveDept()) ){
                        errorMessage.add("第" + lineNumber + "行，接收方部门为空");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getProvideBy()) ){
                        errorMessage.add("第" + lineNumber + "行，提供人为空");
                        errorLines++;
                        continue;
                    }
                    if(null == template.getReceiveBy()){
                        errorMessage.add("第" + lineNumber + "行，接收人为空");
                        errorLines++;
                        continue;
                    }
                    if(null != template.getTargetValue() && !NumberUtils.isNumber(template.getTargetValue())){
                        errorMessage.add("第" + lineNumber + "行，目标值只能为数字");
                        errorLines++;
                        continue;
                    }

                    //情况创建、修改人不让导入。使用默认值
                    template.setCreateBy(null);
                    template.setUpdateBy(null);
                    template.setUpdateTime(null);
                    this.saveOrUpdate(template);
                    successLines++;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                errorLines++;
                errorMessage.add("发生异常：" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
    }
}
