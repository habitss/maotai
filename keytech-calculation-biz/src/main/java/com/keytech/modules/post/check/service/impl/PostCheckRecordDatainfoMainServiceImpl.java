package com.keytech.modules.post.check.service.impl;

import cn.hutool.core.util.IdUtil;
import com.keytech.modules.post.check.entity.PostCheckRecordDatainfoDetail;
import com.keytech.modules.post.check.entity.PostCheckRecordDatainfoMain;
import com.keytech.modules.post.check.mapper.PostCheckRecordDatainfoMainMapper;
import com.keytech.modules.post.check.service.IPostCheckRecordDatainfoDetailService;
import com.keytech.modules.post.check.service.IPostCheckRecordDatainfoMainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

/**
 * @Description: 岗位记录填报主表
 * @Author: jeecg-boot
 * @Date:   2025-02-08
 * @Version: V1.0
 */
@Service
public class PostCheckRecordDatainfoMainServiceImpl extends BaseServiceImpl<PostCheckRecordDatainfoMainMapper, PostCheckRecordDatainfoMain> implements IPostCheckRecordDatainfoMainService {
    @Resource
    private IPostCheckRecordDatainfoDetailService iPostCheckRecordDatainfoDetailService;

    @Override
    public void saveDataInfo(PostCheckRecordDatainfoMain postCheckRecordDatainfoMain) {
        //判断是否新增数据，如果是新增，则生成id
        if(null == postCheckRecordDatainfoMain.getId()){
            postCheckRecordDatainfoMain.setId(IdUtil.getSnowflakeNextIdStr());
        }
        for(PostCheckRecordDatainfoDetail detail : postCheckRecordDatainfoMain.getDetailList()){
            if(detail.getId().indexOf("temp_")>=0){
                detail.setId(null);
                detail.setDatainfoId(postCheckRecordDatainfoMain.getId());
            }
        }
        this.saveOrUpdate(postCheckRecordDatainfoMain);
        iPostCheckRecordDatainfoDetailService.saveOrUpdateBatch(postCheckRecordDatainfoMain.getDetailList());
    }
}
