package com.keytech.modules.i18n.mapper;

import com.keytech.modules.i18n.entity.MdmI18nEntryLang;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.i18n.vo.I18nEntryLangVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 国际化词条翻译
 * @author yepeng
 * @since 2025.12.16
 * */
@Mapper
public interface MdmI18nEntryLangMapper extends BaseMapper<MdmI18nEntryLang> {

    /**
     * 根据词条id查询词条翻译
     * @param entryIds 词条id列表
     * @since 2025.12.18
     */
    List<Map<String, Object>> selectByEntryIds(@Param("entryIds") List<Integer> entryIds);

    /**
     * 批量插入或更新词条翻译
     * @param list 词条翻译列表
     * @since 2025.12.18
     */
    void batchInsertOrUpdate(@Param("list") List<MdmI18nEntryLang> list);

    /**
     * 批量删除词条翻译
     * @param entryId 词条id
     * @param langCodes 语言列表
     * @since 2025.12.18
     */
    void batchDeleteByEntryIdAndLangCodes(Integer entryId, List<String> langCodes);

    /**
     * 查询指定语言的词条翻译
     * @param langCode 语言代码
     * @since 2025.12.18
     */
    @Select("SELECT e.entry_key AS entryKey, el.content FROM mes3_mdm_i18n_entry e JOIN mes3_mdm_i18n_entry_lang el " +
            "ON e.id = el.entry_id WHERE e.del_flag = 0 AND el.del_flag = 0 AND el.lang_code = #{langCode}")
    List<I18nEntryLangVO> selectByLangCode(@Param("langCode") String langCode);

}
