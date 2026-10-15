package com.keytech.modules.pi.mapper;

import com.keytech.modules.pi.entity.SourceSistarRtStepParams;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiRaUnit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: RA-设备字典
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Mapper
public interface PiRaUnitMapper extends BaseMapper<PiRaUnit> {


    /**
     * 获取设备
     * @param offset
     * @param pageSize
     * @return
     */
    @Select(" SELECT UID as id ,UID as uid,AreaUID as area_uid,UnitID as unit_id,NameCN as name_cn,NameEN as name_en, " +
            " DescCN as desc_cn,DescEN as desc_en,Version1 as version1,Version2 as version2,VersionInfo as version_info, " +
            " BK1 as bk1,BK2 as bk2,AccessPath as access_path,ImageKey as image_key,IsRecipeable as is_recipeable FROM Model_Unit "+
            " ORDER BY UnitID,AreaUID " +
            " OFFSET #{offset} ROWS " +
            " FETCH NEXT #{pageSize} ROWS ONLY")
    List<PiRaUnit> deviceDataExtract(
            @Param("offset") long offset,
            @Param("pageSize") long pageSize);

}
