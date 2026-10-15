package com.keytech.modules.opl.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.opl.entity.OplReviewPlan;
import com.keytech.modules.opl.entity.OplReviewPlanFile;
import com.keytech.modules.opl.mapper.OplReviewPlanFileMapper;
import com.keytech.modules.opl.service.IOplReviewPlanFileService;
import com.keytech.modules.opl.service.IOplReviewPlanService;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: Opl管理回顾-文件
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
@Service
public class OplReviewPlanFileServiceImpl extends BaseServiceImpl<OplReviewPlanFileMapper, OplReviewPlanFile> implements IOplReviewPlanFileService {

    @Autowired
    private IOplReviewPlanService oplReviewPlanService;

    @Override
    public Result<?> fileSave(JSONObject input) {
        String id = input.getString("id");
        JSONArray oplDescList = input.getJSONArray("oplDescList");
        JSONObject jsonObject = oplDescList.getJSONObject(0);
        OplReviewPlan mdmOplReviewPlan = oplReviewPlanService.getById(id);
        String json = mdmOplReviewPlan.getResultData();
        OplReviewPlanFile mdmOplReviewPlanFile= new OplReviewPlanFile();
        try {
            //判断是否更新版本和施工 当前周是否大于之前周
            if(oplReviewPlanService.isUptodate(id,input.get("week")+"")){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = sdf.parse(jsonObject.get("operateTime")+"");
                mdmOplReviewPlanFile.setOperateTime(date);
                mdmOplReviewPlan.setOperateTime(date); //实施时间
                mdmOplReviewPlan.setLatestVersionNumber(jsonObject.get("version")+""); //版本号
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        //json 字符转json对象
        JSONObject jsonObjectPlan = JSON.parseObject(json);
        jsonObjectPlan.put(input.get("week")+"", "3");
        mdmOplReviewPlan.setResultData(jsonObjectPlan.toJSONString());
        oplReviewPlanService.updateById(mdmOplReviewPlan);

        mdmOplReviewPlanFile.setReviewPlanId(id);
        mdmOplReviewPlanFile.setReview(jsonObject.get("review")+"");
        mdmOplReviewPlanFile.setVersion(jsonObject.get("version")+"");

        mdmOplReviewPlanFile.setChanges(jsonObject.get("changes")+"");
        mdmOplReviewPlanFile.setWeek(input.get("week")+"");
        mdmOplReviewPlanFile.setFileName(jsonObject.get("fileName")+"");
        mdmOplReviewPlanFile.setUrl(jsonObject.get("url")+"");
        save(mdmOplReviewPlanFile);
        return Result.OK(input);
    }
}
