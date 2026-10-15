package com.keytech.modules.i18n.mapper;

import com.keytech.modules.i18n.entity.MdmI18nEntry;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 国际化词条
 * @author yepeng
 * @since 2025.12.16
 * */
@Mapper
public interface MdmI18nEntryMapper extends BaseMapper<MdmI18nEntry> {

    /**
     * 批量插入或更新词条
     * @param list 词条列表
     * @since 2025.12.18
     */
    void batchInsertOrUpdate(@Param("list") List<MdmI18nEntry> list);

}
