package com.keytech.modules.serviceAgreement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.fivewhy.entity.FiveWhyDetail;
import com.keytech.modules.fivewhy.service.IFiveWhyAnalyseService;
import com.keytech.modules.fivewhy.service.IFiveWhyDetailService;
import com.keytech.modules.serviceAgreement.dto.AgreementExceptionDto;
import com.keytech.modules.serviceAgreement.entity.ServiceAgreementException;
import com.keytech.modules.serviceAgreement.mapper.ServiceAgreementExceptionMapper;
import com.keytech.modules.serviceAgreement.service.IServiceAgreementExceptionService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 服务协议异常记录表
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Service
public class ServiceAgreementExceptionServiceImpl extends BaseServiceImpl<ServiceAgreementExceptionMapper, ServiceAgreementException> implements IServiceAgreementExceptionService {

    @Autowired
    private IFiveWhyAnalyseService fiveWhyAnalyseService;

    @Autowired
    private IActionPlanTrackService actionPlanTrackService;

    @Autowired
    private IFiveWhyDetailService fiveWhyDetailService;

    @Override
    public List<AgreementExceptionDto> listSt(Set<String> keySet, int year) {
        List<ServiceAgreementException> list = this.list(new LambdaQueryWrapper<ServiceAgreementException>().likeRight(ServiceAgreementException::getExceptionDate,year)
                .in(ServiceAgreementException::getServiceAgreementId, keySet));
        Map<String, List<ServiceAgreementException>> exceptionStMap = list.stream()
                .collect(Collectors.groupingBy(ServiceAgreementException::getServiceAgreementId));
        List<AgreementExceptionDto> dtoList = new ArrayList<>();
        exceptionStMap.forEach((key, exception) -> {
            // 按月份分组求和
            Map<Integer, Long> sumByMonth = exception.stream()
                    .collect(Collectors.groupingBy(
                            obj -> DateUtils.getMonthByDate(obj.getExceptionDate()), // 提取年月部分作为键
                            Collectors.counting() // 求和
                    ));
            AgreementExceptionDto agreementExceptionDto = new AgreementExceptionDto();
            agreementExceptionDto.setServiceAgreementId(key);
            agreementExceptionDto.setYear(year);
            if(null != sumByMonth.get(1)) {
                agreementExceptionDto.setJan(sumByMonth.get(1).intValue());
            } else {
                agreementExceptionDto.setJan(0);
            }
            if(null != sumByMonth.get(2)) {
                agreementExceptionDto.setFeb(sumByMonth.get(2).intValue());
            } else {
                agreementExceptionDto.setFeb(0);
            }
            if(null != sumByMonth.get(3)) {
                agreementExceptionDto.setMar(sumByMonth.get(3).intValue());
            } else {
                agreementExceptionDto.setMar(0);
            }
            if(null != sumByMonth.get(4)) {
                agreementExceptionDto.setApr(sumByMonth.get(4).intValue());
            } else {
                agreementExceptionDto.setApr(0);
            }
            if(null != sumByMonth.get(5)) {
                agreementExceptionDto.setMay(sumByMonth.get(5).intValue());
            } else {
                agreementExceptionDto.setMay(0);
            }
            if(null != sumByMonth.get(6)) {
                agreementExceptionDto.setJun(sumByMonth.get(6).intValue());
            } else {
                agreementExceptionDto.setJun(0);
            }
            if(null != sumByMonth.get(7)) {
                agreementExceptionDto.setJul(sumByMonth.get(7).intValue());
            } else {
                agreementExceptionDto.setJul(0);
            }
            if(null != sumByMonth.get(8)) {
                agreementExceptionDto.setAug(sumByMonth.get(8).intValue());
            } else {
                agreementExceptionDto.setAug(0);
            }
            if(null != sumByMonth.get(9)) {
                agreementExceptionDto.setSep(sumByMonth.get(9).intValue());
            } else {
                agreementExceptionDto.setSep(0);
            }
            if(null != sumByMonth.get(10)) {
                agreementExceptionDto.setOct(sumByMonth.get(10).intValue());
            } else {
                agreementExceptionDto.setOct(0);
            }
            if(null != sumByMonth.get(11)) {
                agreementExceptionDto.setNov(sumByMonth.get(11).intValue());
            } else {
                agreementExceptionDto.setNov(0);
            }
            if(null != sumByMonth.get(12)) {
                agreementExceptionDto.setDec(sumByMonth.get(12).intValue());
            } else {
                agreementExceptionDto.setDec(0);
            }
            dtoList.add(agreementExceptionDto);
        });

        //实际值
        return dtoList;
    }

    @Override
    public IPage<ServiceAgreementException> queryPageList(Page<ServiceAgreementException> page,
                                                          QueryWrapper<ServiceAgreementException> queryWrapper, HttpServletRequest req) {

        IPage<ServiceAgreementException> pageList = page(page, queryWrapper);
        List<ServiceAgreementException> exceptionList = pageList.getRecords();
        //List<ServiceAgreementException> result = new ArrayList();
        //lims与5why对照
        Map<String,String> fwhymap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(exceptionList)){
            List<String> exceptionIds = exceptionList.stream().map(ServiceAgreementException::getId).toList();
            //获取行动
                //获取5why
            LambdaQueryWrapper<FiveWhyAnalyse> fwhyQuery = new LambdaQueryWrapper<>();
            exceptionIds.forEach(id -> fwhyQuery.or().like(FiveWhyAnalyse::getDataId, id));
            List<FiveWhyAnalyse> fiveWhyAnalyseList = fiveWhyAnalyseService.list(fwhyQuery);
            fwhymap = fiveWhyAnalyseList.stream().collect(Collectors.toMap(
                        FiveWhyAnalyse::getDataId,
                        FiveWhyAnalyse::getId,
                        (existing, replacement) -> existing
                ));

            List<FiveWhyDetail> actionPlanTrackList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(fiveWhyAnalyseList)){
                    //获取行动(行动计划跟踪)
                LambdaQueryWrapper<FiveWhyDetail> query = new LambdaQueryWrapper<>();
                    //5whyID
                fiveWhyAnalyseList.forEach(fiveWhyAnalyse -> query.or().like(FiveWhyDetail::getRecordId, fiveWhyAnalyse.getId()));
                query.eq(FiveWhyDetail::getActionType,true);
                actionPlanTrackList = fiveWhyDetailService.list(query);
                for(ServiceAgreementException serviceAgreementException : exceptionList){
                    String id = serviceAgreementException.getId();
                    //5w
                    String fid = fwhymap.get(id);
                    if(StringUtils.isNotEmpty(fid)){
                        serviceAgreementException.setAnalysisId(fid);
                        serviceAgreementException.setFiveWhyStatus(1);
                        if(CollectionUtils.isNotEmpty(actionPlanTrackList)){
                            serviceAgreementException.setActionStatus(1);
                        }
                    }
                    this.updateById(serviceAgreementException);
                }
            }

        } else {
            return pageList;
        }

        return pageList.setRecords(exceptionList);
    }

}
