package com.keytech.modules.manage.check.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.base.service.BaseService;
import com.keytech.modules.home.dto.StatisticalVo;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.manage.check.dto.ManageCheckListDto;
import com.keytech.modules.manage.check.dto.ManageCheckListSummaryDto;
import com.keytech.modules.manage.check.entity.ManageCheckList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description: 管理者检查清单-分层检查清单
 * @Author: jeecg-boot
 * @Date:   2025-03-31
 * @Version: V1.0
 */
public interface IManageCheckListService extends BaseService<ManageCheckList> {

    /**
     * 分页查询
     * @param manageCheckList
     * @param pageNo
     * @param pageSize
     * @return
     */
    IPage<ManageCheckList> queryPageList(ManageCheckList manageCheckList, Integer pageNo, Integer pageSize);

    /**
     * 新增分层检查清单
     * @param dto
     * @return
     */
    Result<String> saveManageCheckList(ManageCheckListDto dto);

    /**
     * 修改分层检查清单
     * @param dto
     * @return
     */
    Result<String> updateManageCheckList(ManageCheckListDto dto);

    /**
     * 批量删除
     * @param deleteList
     */
    void deleteBatch(List<ManageCheckList> deleteList);

    /**
     * 导入数据
     * @param request
     * @param response
     * @param clazz
     */
    Result<?> importData(HttpServletRequest request, HttpServletResponse response, Class<ManageCheckList> clazz) throws IOException;

    /**
     * 查询汇总列表
     * @param manageCheckList
     * @return
     */
    Map<String, List<ManageCheckListSummaryDto>> querySummaryList(ManageCheckList manageCheckList);

    /**
     * 查询首页汇总
     * @param dto
     * @return
     */
    JSONObject queryIndexSummary(WSActionStatisticsDto dto);

    /**
     * 查询首页汇总
     * @param dto
     * @return
     */
    List<StatisticalVo> queryIndexSummaryList(WSActionStatisticsDto dto);
}
