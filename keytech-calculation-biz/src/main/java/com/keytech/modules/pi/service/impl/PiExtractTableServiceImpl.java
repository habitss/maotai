package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.pi.entity.PiExtractTable;
import com.keytech.modules.pi.entity.PiExtractTableField;
import com.keytech.modules.pi.mapper.PiExtractTableMapper;
import com.keytech.modules.pi.service.IPiExtractTableFieldService;
import com.keytech.modules.pi.service.IPiExtractTableService;
import com.keytech.modules.pi.vo.TableInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: PI-数据抽取配置主表
 * @Author: jeecg-boot
 * @Date:   2025-08-25
 * @Version: V1.0
 */
@Service
public class PiExtractTableServiceImpl extends BaseServiceImpl<PiExtractTableMapper, PiExtractTable> implements IPiExtractTableService {

    @Autowired
    private PiExtractTableMapper piExtractTableMapper;
    @Autowired
    private IPiExtractTableFieldService iPiExtractTableFieldService;


    /**
     * 数据抽取测试
     */
    @Override
    public void extractTest() {
        String table = "mes3_pi_table_001";
        //查询bmsqlservice数据源数据
        List<Map<String, Object>> list = piExtractTableMapper.extractTest(table,"bmsqlservice");
        for (Map<String, Object> map : list) {
            System.out.println(map);
        }
        System.out.println("数据抽取完成");
    }

    /**
     * 获取表信息列表
     *
     * @return 表信息VO对象列表
     */
    public List<TableInfoVo> getTable() {

//        List<TableInfoVO> list = piExtractTableMapper.getTable();
        List<TableInfoVo> list = new ArrayList<>();
        if(list != null && list.isEmpty()){
            //生成测试数据
            TableInfoVo tableInfoVO = new TableInfoVo();
            tableInfoVO.setTableName("mes3_pi_table_001");
            tableInfoVO.setTableComment("测试表1");
            list.add(tableInfoVO);

            TableInfoVo tableInfoVo2 = new TableInfoVo();
            tableInfoVo2.setTableName("mes3_pi_table_002");
            tableInfoVo2.setTableComment("测试表2");
            list.add(tableInfoVo2);

            TableInfoVo tableInfoVo3 = new TableInfoVo();
            tableInfoVo3.setTableName("mes3_pi_table_003");
            tableInfoVo3.setTableComment("测试表3");
            list.add(tableInfoVo3);
        }
        return list;
    }


    /**
     * 获取目标表信息列表
     *
     * @return 目标表信息VO对象列表
     */
    @Override
    public List<TableInfoVo> getTargetTable() {
        List<TableInfoVo> list = piExtractTableMapper.getTargetTable();
        for (TableInfoVo tableInfoVO : list) {
            if (StrUtil.isBlank(tableInfoVO.getTableComment())){
                tableInfoVO.setTableComment(tableInfoVO.getTableName());
            }
        }
        return list;
    }

    /**
     * 存储分配信息
     */
    @Override
    public void add(PiExtractTable piExtractTable) {
        List<PiExtractTableField> fields = piExtractTable.getFields();
        //存储配置主表
        saveOrUpdate(piExtractTable);
        for (PiExtractTableField field : fields) {
            field.setExtractTableId(piExtractTable.getId());
        }
        iPiExtractTableFieldService.saveOrUpdateBatch(fields);
    }

    /**
     * 数据抽取
     * 1. 抽取 sistar_rt_step_params
     * 2. 抽取 sistar_rt_steps
     * 3. 抽取 字典
     */
    @Override
    public void extractData() {
        //1. 抽取 sistar_rt_step_params



    }

}
