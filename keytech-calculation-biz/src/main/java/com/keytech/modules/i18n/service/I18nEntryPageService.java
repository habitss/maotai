package com.keytech.modules.i18n.service;

import com.keytech.modules.i18n.dto.I18nEntryPageResult;
import com.keytech.modules.i18n.dto.I18nEntryRowDto;
import com.keytech.modules.i18n.entity.MdmI18nEntryLang;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 词条维度分页查询及行保存服务
 * @author yepeng
 * @since 2025.12.18
 */
public interface I18nEntryPageService {

    /**
     * 词条维度分页查询（横向多语言）
     * @author yepeng
     * @since 2025.12.18
     */
    I18nEntryPageResult pageEntryWithLang(String entryKey, Integer pageNo, Integer pageSize);


    /**
     * 保存单行（一个词条的所有语言）
     * @author yepeng
     * @since 2025.12.18
     */
    void saveRow(I18nEntryRowDto rowDto);

    /**
     * 国际化词条多语言翻译 导出Excel
     * @author yepeng
     * @since 2025.12.19
     */
    void exportExcel(HttpServletResponse response, String entryDesc, String fileName) throws IOException;

    /**
     * 国际化词条多语言翻译 导入Excel
     * @author yepeng
     * @since 2025.12.19
     */
    Result<String> importExcel(MultipartFile file) throws IOException;

}