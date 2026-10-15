package com.keytech.modules.line.efficiency.mapper;

import com.keytech.modules.line.efficiency.entity.LineEfficiencyHandle;
import com.keytech.modules.line.efficiency.dto.EqutalarmrecDto;
import com.keytech.modules.line.efficiency.dto.EqutoeerecDto;
import com.keytech.modules.line.efficiency.dto.EqutrunrecDto;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 线效率-产量/线效状态点位配置
 * @Author: jeecg-boot
 * @Date:   2025-05-26
 * @Version: V1.0
 */
@Mapper
public interface LineEfficiencyHandleMapper extends BaseMapper<LineEfficiencyHandle> {

    /**
     * 设备运行时长-数据批量写入
     * @param entityList
     * @return
     */
    @Insert({"<script>",
            "INSERT INTO dg_batchdata.equtrunrec (Status, orgId, orgName,equtID,equtName,type,begTime,endTime,runTime) VALUES ",
            "<foreach collection='list' item='item' index='index' separator=','>",
            "(#{item.status}, #{item.orgId}, #{item.orgName}, #{item.equtID}, #{item.equtName}, #{item.type}, #{item.begTime}, #{item.endTime}, #{item.runTime})",
            "</foreach>",
            "</script>"})
    int insertEqutrunrecBatch(@Param("list") List<EqutrunrecDto> entityList);



    /**
     * 报警记录-数据批量写入
     * @param entityList
     * @return
     */
    @Insert({"<script>",
            "INSERT INTO dg_batchdata.equtalarmrec (Status, orgId,equtId,equtName,alarmName,alarmBegTime,alarmEndTime,alarmRunTime,alarmtype) VALUES ",
            "<foreach collection='list' item='item' index='index' separator=','>",
            "(#{item.status}, #{item.orgId}, #{item.equtId}, #{item.equtName}, #{item.alarmName}, #{item.alarmBegTime}, #{item.alarmEndTime}, #{item.alarmRunTime}, #{item.alarmtype})",
            "</foreach>",
            "</script>"})
    int insertalarmMinuteBatch(@Param("list") List<EqutalarmrecDto> entityList);

    /**
     * OEE-数据批量写入
     * @param entityList
     * @return
     */
    @Insert({"<script>",
            "INSERT INTO dg_batchdata.equtoeerec (Status, orgId,lineBTime,lineETime,bBBottles,actRunLength,OEE,LineCap) VALUES ",
            "<foreach collection='list' item='item' index='index' separator=','>",
            "(#{item.status}, #{item.orgId}, #{item.lineBTime}, #{item.lineETime}, #{item.bBBottles}, #{item.actRunLength}, #{item.OEE}, #{item.LineCap})",
            "</foreach>",
            "</script>"})
    int insertOeeBatch(@Param("list") List<EqutoeerecDto> entityList);

}
