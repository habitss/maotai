package com.keytech.modules.pi.mapper;

import com.keytech.modules.pi.entity.PiRaBop;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiRaProcedure;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: RA-配方
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Mapper
public interface PiRaProcedureMapper extends BaseMapper<PiRaProcedure> {

    /**
     * 配方数据查询
     */
    @Select(" select UID as id, UID as uid,AreaUID as area_uid,NameCN as name_cn,NameEN as name_en,DescCN as desc_cn, " +
            " DescEN as desc_en,Prefix as prefix,VersionInfo as version_info,Version1 as version1,Version2 as version2, " +
            " BK1 as bk1,BK2 as bk2,AccessPath as access_path,ImageKey as image_key,BKProcessID as bk_process_id, " +
            " ID as tid,Type as type from Recipe_Procedure "+
            " ORDER BY UID,AreaUID " +
            " OFFSET #{offset} ROWS " +
            " FETCH NEXT #{pageSize} ROWS ONLY")
    List<PiRaProcedure> recipeDataExtract(
            @Param("offset") long offset,
            @Param("pageSize") long pageSize);


}
