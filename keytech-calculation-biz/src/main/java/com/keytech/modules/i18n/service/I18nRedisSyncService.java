package com.keytech.modules.i18n.service;

import java.util.Map;

/**
 * 国际化词条多语言翻译 同步Redis服务
 * @author yepeng
 * @since 2025.12.21
 */
public interface I18nRedisSyncService {
    /**
     * 全量同步所有语言到 Redis
     * @author yepeng
     * @since 2025.12.21
     */
    void fullSyncAll();


    /**
     * 同步指定语言
     * @param langCode 语言代码
     * @author yepeng
     * @since 2025.12.21
     */
    void fullSyncByLang(String langCode);

    /**
     * 更新单个词条的某个语言
     * @param entryKey 词条key
     * @param langCode 语言代码
     * @param content 翻译内容
     * @author yepeng
     * @since 2025.12.21
     */
    void updateEntryLang(String entryKey, String langCode, String content);


    /**
     * 批量更新某语言下的多个词条
     * @param langCode 语言代码
     * @param entryMap 词条Map
     * @author yepeng
     * @since 2025.12.21
     */
    void batchUpdateLangCode(String langCode, Map<String, String> entryMap);

    /**
     * 批量更新某语言下的多个词条
     * @param entryKey 词条Key
     * @param langMap 词条Map
     * @author yepeng
     * @since 2025.12.21
     */
    void batchUpdateByEntryKey(String entryKey, Map<String, String> langMap);

    /**
     * 构建Redis语言 ZIP
     * @param langCode 语言代码
     * @author yepeng
     * @since 2025.12.21
     */
    byte[] buildRedisLangZip(String langCode);

    /**
     * 从Redis中获取所有语言版本号
     * @return 语言版本号
     * @author yepeng
     * @since 2025.12.22
     */
    Map<String, Long> getLangVersions();

}
