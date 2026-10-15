package com.keytech.modules.pi.mapper;

import com.keytech.modules.pi.entity.PiRaUnit;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiRaBop;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: RA-步骤字典
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Mapper
public interface PiRaBopMapper extends BaseMapper<PiRaBop> {

    /**
     * 步骤数据查询
     */
    @Select(" select UID as id, UID as uid,UnitUID as unit_uid,BopID as bop_id,NameCN as name_cn,NameEN as name_en,DescCN as desc_cn, " +
            " DescEN as desc_en,Version1 as version1,Version2 as version2,VersionInfo as version_info,BK1 as bk1, " +
            " BK2 as bk2,AccessPath as access_path,ImageKey as image_key from Model_Bop "+
            " ORDER BY BopID,UnitUID " +
            " OFFSET #{offset} ROWS " +
            " FETCH NEXT #{pageSize} ROWS ONLY")
    List<PiRaBop> stepDataExtract(
            @Param("offset") long offset,
            @Param("pageSize") long pageSize);






}
