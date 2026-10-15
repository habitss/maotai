package com.keytech.modules.pi.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.dto.AlgorithmDataDto;
import com.keytech.modules.pi.dto.TableDataDto;
import com.keytech.modules.pi.dto.TransferDataDto;
import com.keytech.modules.pi.entity.PiAllocationTable;
import com.keytech.modules.pi.entity.PiAllocationTableField;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;

import java.util.List;
import java.util.Map;

/**
 * @Description: 报表字段分配
 * @Author: jeecg-boot
 * @Date:   2024-11-27
 * @Version: V1.0
 */
public interface IPiAllocationTableFieldService extends  BaseService<PiAllocationTableField> {

    /**获取分配的字段*/
    List<TransferDataDto> getAllocationField(PiAllocationTable piAllocationTable);

    /**报表字段分配*/
    void fieldSave(PiAllocationTableField piAllocationTableField);

    /**获取报表数据*/
    Map<String, Object> queryData(Integer pageNo, Integer pageSize, HttpServletRequest req,PiAllocationTableField piAllocationTableField);

    /**更新报表*/
    Boolean editVal(Map map);

    /**更新报表*/
    Map<String,String> batchEditVal(Map map);

    /**
     * 获取报表IP
     * @param tableDataDto
     * @return
     */
    List<?> tableHead(TableDataDto tableDataDto);

    List<?> getAlgorithmField(PiAllocationTableField piAllocationTableField);


    /**
     * 查询数据总数
     * @param paramMap
     * @return
     */
    int getTableDataCount(Map<String, Object> paramMap);

    /**
     * 分页查询
     * @param paramMap
     * @return
     */
    List<Map<String, Object>> getTableData(Map<String, Object> paramMap);

    /**
     * 开启/关闭配置算法的编辑状态
     * @param allocationTableId
     * @param isEdit
     */
    void openOrCloseEdit(String allocationTableId, String isEdit);

    /**
     * 获取字段最新10条数据
     * @param piAllocationTableField
     * @return
     */
    Map<String, Object> getTableFieldData(PiAllocationTableField piAllocationTableField);

    /**
     * 更新PI间数据
     * @param dataIds
     * @param piTable
     * @param piCodes
     */
    void savePICalculateSignal(List<String> dataIds, String piTable,List<String> piCodes);

    /**
     * 更新字段数据
     * @param piAllocationTableField
     * @return
     */
    Result<?> edit(PiAllocationTableField piAllocationTableField);
}
