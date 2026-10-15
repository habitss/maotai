package com.keytech.modules.i18n.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.i18n.entity.MdmI18nLang;
import com.keytech.modules.i18n.mapper.MdmI18nEntryLangMapper;
import com.keytech.modules.i18n.mapper.MdmI18nLangMapper;
import com.keytech.modules.i18n.service.I18nRedisSyncService;
import com.keytech.modules.i18n.vo.I18nEntryLangVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 国际化词条多语言翻译 同步Redis服务
 * @author yepeng
 * @since 2025.12.21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class I18nRedisSyncServiceImpl implements I18nRedisSyncService {
    private final MdmI18nLangMapper mdmI18nLangMapper;
    private final MdmI18nEntryLangMapper mdmI18nEntryLangMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 全量同步所有语言到 Redis
     * @author yepeng
     * @since 2025.12.21
     */
    @Override
    public void fullSyncAll() {
        List<MdmI18nLang> langList = mdmI18nLangMapper.selectList(null);
        for (MdmI18nLang lang : langList) {
            this.fullSyncByLang(lang.getLangCode());
        }
    }

    /**
     * 同步指定语言 到 Redis
     * @param langCode 语言代码
     * @author yepeng
     * @since 2025.12.21
     */
    @Override
    public void fullSyncByLang(String langCode) {
        String redisKey = "i18n:lang:" + langCode;

        // 1. 查询该语言下所有词条翻译
        List<I18nEntryLangVO> list = mdmI18nEntryLangMapper.selectByLangCode(langCode);
        if (list.isEmpty()) {
            return;
        }

        // 2. 构建 Redis Hash
        Map<String, String> hash = new HashMap<>(list.size());
        for (I18nEntryLangVO vo : list) {
            hash.put(vo.getEntryKey(), vo.getContent());
        }

        // 3. 覆盖写入（保证一致性）
        redisTemplate.delete(redisKey);
        redisTemplate.opsForHash().putAll(redisKey, hash);

        // 4. 加载版本号
        MdmI18nLang lang = mdmI18nLangMapper.selectOne(Wrappers.<MdmI18nLang>lambdaQuery().eq(MdmI18nLang::getLangCode, langCode));
        redisTemplate.opsForValue().set("i18n:version:" + langCode, String.valueOf(lang.getVersion()));
    }

    /**
     * 更新单个词条的某个语言
     * @param entryKey 词条key
     * @param langCode 语言代码
     * @param content 翻译内容
     * @author yepeng
     * @since 2025.12.21
     */
    @Override
    public void updateEntryLang(String entryKey, String langCode, String content) {
        String redisKey = "i18n:lang:" + langCode;
        // 更新 Hash 中的单个字段
        redisTemplate.opsForHash().put(redisKey, entryKey, content);
        // 刷新该语言版本号
        refreshVersion(langCode);
    }

    /**
     * 批量更新某语言下的多个词条
     * @param langCode 语言代码
     * @param entryMap 词条Map
     * @author yepeng
     * @since 2025.12.21
     */
    @Override
    public void batchUpdateLangCode(String langCode, Map<String, String> entryMap) {
        String redisKey = "i18n:lang:" + langCode;
        redisTemplate.opsForHash().putAll(redisKey, entryMap);
        // 刷新该语言版本号
        refreshVersion(langCode);
    }

    /**
     * 批量更新某语言下的多个词条
     * @param entryKey 词条Key
     * @param langMap 词条Map
     * @author yepeng
     * @since 2025.12.21
     */
    @Override
    public void batchUpdateByEntryKey(String entryKey, Map<String, String> langMap) {
        langMap.keySet().forEach(langCode -> this.updateEntryLang(entryKey, langCode, langMap.get(langCode)));
    }

    /**
     * 刷新版本号
     * @param langCode 语言代码
     */
    private void refreshVersion(String langCode) {
        long version = System.currentTimeMillis();
        mdmI18nLangMapper.update(null, Wrappers.<MdmI18nLang>lambdaUpdate().eq(MdmI18nLang::getLangCode, langCode).set(MdmI18nLang::getVersion, version));
        redisTemplate.opsForValue().set("i18n:version:" + langCode, String.valueOf(version));
    }

    /**
     * 从Redis中获取所有语言版本号
     * @return 语言版本号
     * @author yepeng
     * @since 2025.12.22
     */
    @Override
    public Map<String, Long> getLangVersions() {
        Set<String> keys = redisTemplate.keys("i18n:version:*");
        Map<String, Long> langVersions = new HashMap<>();
        for (String key : keys) {
            String langCode = key.replace("i18n:version:", "");
            String version = redisTemplate.opsForValue().get(key);
            langVersions.put(langCode, StringUtils.isBlank(version) ? 0L : Long.parseLong(version));
        }
        return langVersions;
    }

    /**
     * 构建Redis语言 ZIP
     * @param langCode 语言代码
     * @author yepeng
     * @since 2025.12.21
     */
    @Override
    public byte[] buildRedisLangZip(String langCode) {
        String redisKey = "i18n:lang:" + langCode;
        // 从 Redis 取全量词条
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);
        if (entries.isEmpty()) {
            // 没有数据也要返回一个合法 zip
            entries = Collections.emptyMap();
        }

        // 转成 Map<String, String>
        Map<String, String> langMap = new LinkedHashMap<>(entries.size());
        for (Map.Entry<Object, Object> e : entries.entrySet()) {
            langMap.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
        }
        // Map 转 JSON
        String json = JSON.toJSONString(langMap);
        // 写 ZIP（内存）
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)
        ) {
            // 关键：entry 名要和前端一致
            ZipEntry zipEntry = new ZipEntry(langCode + ".json");
            zos.putNextEntry(zipEntry);
            byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
            zos.write(jsonBytes);
            zos.closeEntry();
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("构建语言 ZIP 失败", e);
            return null;
        }
    }

}
