package com.keytech.modules.sop.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.sop.entity.SopReviewPlan;
import com.keytech.modules.sop.entity.SopReviewPlanFile;
import com.keytech.modules.sop.mapper.SopReviewPlanFileMapper;
import com.keytech.modules.sop.service.ISopHomePageService;
import com.keytech.modules.sop.service.ISopReviewPlanFileService;
import com.keytech.modules.sop.service.ISopReviewPlanService;
import org.checkerframework.checker.units.qual.A;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: SOP管理回顾-文件
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
@Service
public class SopReviewPlanFileServiceImpl extends BaseServiceImpl<SopReviewPlanFileMapper, SopReviewPlanFile> implements ISopReviewPlanFileService {

    @Autowired
    private ISopReviewPlanService sopReviewPlanService;

    @Override
    public Result<?> fileSave(JSONObject input) {
        String id = input.getString("id");
        JSONArray sopDescList = input.getJSONArray("sopDescList");
        JSONObject jsonObject = sopDescList.getJSONObject(0);
        SopReviewPlan mdmSopReviewPlan = sopReviewPlanService.getById(id);
        String json = mdmSopReviewPlan.getResultData();
        SopReviewPlanFile mdmSopReviewPlanFile= new SopReviewPlanFile();
        try {
            //判断是否更新版本和施工 当前周是否大于之前周

            if(sopReviewPlanService.isUptodate(id,input.get("week")+"")){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = sdf.parse(jsonObject.get("operateTime")+"");
                mdmSopReviewPlanFile.setOperateTime(date);
                mdmSopReviewPlan.setOperateTime(date); //实施时间
                mdmSopReviewPlan.setLatestVersionNumber(jsonObject.get("version")+""); //版本号
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        //json 字符转json对象
        JSONObject jsonObjectPlan = JSON.parseObject(json);
        jsonObjectPlan.put(input.get("week")+"", "3");
        mdmSopReviewPlan.setResultData(jsonObjectPlan.toJSONString());
        sopReviewPlanService.updateById(mdmSopReviewPlan);

        mdmSopReviewPlanFile.setReviewPlanId(id);
        mdmSopReviewPlanFile.setReview(jsonObject.get("review")+"");
        mdmSopReviewPlanFile.setVersion(jsonObject.get("version")+"");

        mdmSopReviewPlanFile.setChanges(jsonObject.get("changes")+"");
        mdmSopReviewPlanFile.setWeek(input.get("week")+"");
        mdmSopReviewPlanFile.setFileName(jsonObject.get("fileName")+"");
        mdmSopReviewPlanFile.setUrl(jsonObject.get("url")+"");
        save(mdmSopReviewPlanFile);
        return Result.OK(input);
    }
}
