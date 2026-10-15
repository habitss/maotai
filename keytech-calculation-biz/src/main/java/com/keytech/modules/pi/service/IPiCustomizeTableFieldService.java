package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.dto.AlgorithmDataDto;
import com.keytech.modules.pi.dto.SinicizedDictDto;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;

import java.util.List;
import java.util.Map;

/**
 * @Description: pi自定义报表字段
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
public interface IPiCustomizeTableFieldService extends  BaseService<PiCustomizeTableField> {

    Result<String> saveField(PiCustomizeTableField piCustomizeTableField);

    void algorithmAdd(AlgorithmDataDto algorithmDataDto);

    List<SinicizedDictDto> getDictionary();

    Map<String, Object> queryData(Integer pageNo, Integer pageSize, HttpServletRequest req, PiCustomizeTableField piCustomizeTableField);

    Boolean editVal(Map map);

    List<PiCustomizeTableField> fieldByTableIdData(PiCustomizeTableField piCustomizeTableField);

    /**
     * 算法复制
     */
    Result<String> algorithmCopy(PiCustomizeTableField piCustomizeTableField);

    /**
     * 获取排序
     * @param tableId
     * @return
     */
    List<PiCustomizeTableField> getSort(String tableId);


    /**
     * 计算算法配置算法
     */
    Page<PiCustomizeTableField> statisticsAlgorithm(Page<PiCustomizeTableField> list);



    /**
     * 删除字段,同时删除算法
     */
    void deleteBatchData(List<String> ids);

    /**
     * 更新字段/同步PI字段
     * @param piCustomizeTableField
     */
    Result<?> updateByData(PiCustomizeTableField piCustomizeTableField);

    /**
     * 开启/关闭配置算法的编辑状态
     * @param tableId
     * @param isEdit
     */
    void openOrCloseEdit(String tableId, String isEdit);

    /**
     * 获取字段(标记标准是否配置)
     * @param piCustomizeTableField
     * @param parameterMap
     * @return
     */
    List<PiCustomizeTableField> getStandardFieldByTableId(PiCustomizeTableField piCustomizeTableField, Map<String, String[]> parameterMap);

    /**
     * 通过tableId获取字段
     * @param tableId
     * @return
     */
    List<PiCustomizeTableField> getFieldByTableId(String tableId);

    /**
     * 字段和算法合并到指定表
     * @return
     */
    Object mergingFieldsAndAlgorithms();
}
