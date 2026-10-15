package com.keytech.modules.manage.check.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.manage.check.entity.ManageCheckList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 管理者检查清单-分层检查清单
 * @Author: jeecg-boot
 * @Date:   2025-03-31
 * @Version: V1.0
 */
@Mapper
public interface ManageCheckListMapper extends BaseMapper<ManageCheckList> {
    IPage<ManageCheckList> queryPageList(@Param("page") Page<ManageCheckList> page, @Param("params") ManageCheckList params, @Param("actionProblemTypeId") String actionProblemTypeId);
}
