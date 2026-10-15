package com.keytech.modules.post.check.service.impl;

import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.post.check.entity.PostCheckRecordConfigProject;
import com.keytech.modules.post.check.entity.PostCheckRecordConfigStandard;
import com.keytech.modules.post.check.mapper.PostCheckRecordConfigProjectMapper;
import com.keytech.modules.post.check.service.IPostCheckRecordConfigProjectService;
import com.keytech.modules.post.check.service.IPostCheckRecordConfigStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 岗位检查记录配置-检查项目
 * @Author: jeecg-boot
 * @Date:   2025-01-06
 * @Version: V1.0
 */
@Service
public class PostCheckRecordConfigProjectServiceImpl extends BaseServiceImpl<PostCheckRecordConfigProjectMapper, PostCheckRecordConfigProject> implements IPostCheckRecordConfigProjectService {
    @Autowired
    private IPostCheckRecordConfigStandardService postCheckRecordPzJcbzService;
    @Autowired
    private IPostCheckRecordConfigStandardService postCheckRecordConfigStandardService;

    @Override
    public void savePostCheckPz(List<PostCheckRecordConfigProject> list) {
        saveOrUpdateBatch(list);
        //检查标准集合
        List<PostCheckRecordConfigStandard> listJcbz = new ArrayList<>();
        for(PostCheckRecordConfigProject jcxm : list){
            //将保存后的id注入到检查标准中
            List<PostCheckRecordConfigStandard> listJcbz_tepm = jcxm.getListJcbz();
            for(PostCheckRecordConfigStandard jcbzVo : listJcbz_tepm){
                jcbzVo.setJcxmId(jcxm.getId());
            }
            listJcbz.addAll(listJcbz_tepm);
        }
        postCheckRecordPzJcbzService.saveOrUpdateBatch(listJcbz);
    }

    @Override
    @Transactional
    public void deleteBatchData(String ids, String bzIds) {
        //删除标准
        postCheckRecordConfigStandardService.removeByIds(Arrays.asList(bzIds.split(",")));
        //删除项目
        this.lambdaUpdate()
                .set(PostCheckRecordConfigProject::getDelFlag, 1)
                .in(PostCheckRecordConfigProject::getId, Arrays.asList(ids.split(",")))
                .notExists("select 1 from mes3_post_check_record_config_standard s" +
                        " where s.del_flag = 0 and s.jcxm_id = mes3_post_check_record_config_project.id ")
                .update();
    }
}
