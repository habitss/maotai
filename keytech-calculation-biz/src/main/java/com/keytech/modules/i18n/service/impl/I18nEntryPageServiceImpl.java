package com.keytech.modules.i18n.service.impl;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.i18n.dto.I18nColumnWidthHandler;
import com.keytech.modules.i18n.dto.I18nEntryPageResult;
import com.keytech.modules.i18n.dto.I18nEntryRowDto;
import com.keytech.modules.i18n.dto.I18nLangDto;
import com.keytech.modules.i18n.entity.MdmI18nEntry;
import com.keytech.modules.i18n.entity.MdmI18nEntryLang;
import com.keytech.modules.i18n.entity.MdmI18nLang;
import com.keytech.modules.i18n.entity.MdmI18nModule;
import com.keytech.modules.i18n.mapper.MdmI18nEntryLangMapper;
import com.keytech.modules.i18n.mapper.MdmI18nEntryMapper;
import com.keytech.modules.i18n.mapper.MdmI18nLangMapper;
import com.keytech.modules.i18n.mapper.MdmI18nModuleMapper;
import com.keytech.modules.i18n.service.I18nEntryPageService;
import com.keytech.modules.i18n.service.I18nRedisSyncService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 国际化词条多语言翻译分页及行保存服务实现
 * @author yepeng
 * @since 2025.12.18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class I18nEntryPageServiceImpl implements I18nEntryPageService {
    private final MdmI18nEntryMapper mdmI18nEntryMapper;
    private final MdmI18nEntryLangMapper mdmI18nEntryLangMapper;
    private final MdmI18nLangMapper mdmI18nLangMapper;
    private final MdmI18nModuleMapper mdmI18nModuleMapper;
    private final I18nRedisSyncService i18nRedisSyncService;
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    /**
     * 词条维度分页查询（横向多语言）
     */
    @Override
    public I18nEntryPageResult pageEntryWithLang(String entryDesc, Integer pageNo, Integer pageSize) {
        // 查询启用语言（动态列来源）
        List<MdmI18nLang> langs = mdmI18nLangMapper.selectList(new LambdaQueryWrapper<MdmI18nLang>()
                .eq(MdmI18nLang::getEnabled, 1).orderByAsc(MdmI18nLang::getSort).orderByAsc(MdmI18nLang::getId));
        // 组装列 DTO
        List<I18nLangDto> columns = langs.stream().map(l -> {
            I18nLangDto dto = new I18nLangDto();
            dto.setLangCode(l.getLangCode());
            dto.setLangName(l.getLangName());
            return dto;
        }).toList();

        // 查询模块列表
        List<MdmI18nModule> moduleList = mdmI18nModuleMapper.selectList(null);
        Map<String, String> moduleMap = moduleList.stream().collect(Collectors.toMap(MdmI18nModule::getModuleCode, MdmI18nModule::getModuleName));

        // 分页查询词条表（主表）
        Page<MdmI18nEntry> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<MdmI18nEntry> qw = new LambdaQueryWrapper<MdmI18nEntry>().orderByAsc(MdmI18nEntry::getSort).orderByAsc(MdmI18nEntry::getId);
        if (StringUtils.isNotBlank(entryDesc)) {
            qw.and(q -> q.like(MdmI18nEntry::getEntryKey, entryDesc).or().like(MdmI18nEntry::getDescription, entryDesc));
        }
        IPage<MdmI18nEntry> entryPage = mdmI18nEntryMapper.selectPage(page, qw);

        // 组装 Row DTO
        List<I18nEntryRowDto> rows = entryPage.getRecords().stream().map(e -> {
            I18nEntryRowDto dto = new I18nEntryRowDto();
            dto.setEntryId(e.getId());
            dto.setEntryKey(e.getEntryKey());
            dto.setModule(moduleMap.get(e.getModule()));
            dto.setDescription(e.getDescription());

            // 先补齐所有语言列（为空）
            for (MdmI18nLang lang : langs) {
                dto.getLangMap().put(lang.getLangCode(), "");
            }
            return dto;
        }).toList();

        if (rows.isEmpty()) {
            return buildResult(columns, entryPage, rows);
        }

        // 查询这些词条的所有翻译
        List<Integer> entryIds = rows.stream().map(I18nEntryRowDto::getEntryId).toList();
        List<Map<String, Object>> langRows = mdmI18nEntryLangMapper.selectByEntryIds(entryIds);

        // 翻译数据灌入 row.langMap
        Map<Integer, I18nEntryRowDto> rowMap = rows.stream().collect(Collectors.toMap(I18nEntryRowDto::getEntryId, Function.identity()));

        for (Map<String, Object> r : langRows) {
            Integer entryId = ((Number) r.get("entry_id")).intValue();
            String langCode = (String) r.get("lang_code");
            String content = (String) r.get("content");

            I18nEntryRowDto row = rowMap.get(entryId);
            if (row != null) {
                row.getLangMap().put(langCode, content);
            }
        }

        // 返回
        return buildResult(columns, entryPage, rows);
    }

    /**
     * 组装结果
     * @param columns 列 DTO
     * @param entryPage 词条分页
     * @param rows 行 DTO
     */
    private I18nEntryPageResult buildResult(List<I18nLangDto> columns, IPage<MdmI18nEntry> entryPage, List<I18nEntryRowDto> rows) {
        Page<I18nEntryRowDto> resultPage = new Page<>();
        resultPage.setCurrent(entryPage.getCurrent());
        resultPage.setSize(entryPage.getSize());
        resultPage.setTotal(entryPage.getTotal());
        resultPage.setRecords(rows);

        I18nEntryPageResult result = new I18nEntryPageResult();
        result.setColumns(columns);
        result.setPage(resultPage);
        return result;
    }

    /**
     * 保存单行（一个词条的所有语言）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRow(I18nEntryRowDto rowDto) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Integer entryId = rowDto.getEntryId();
        Map<String, String> langMap = rowDto.getLangMap();
        if (langMap == null || langMap.isEmpty()) {
            return;
        }

        // 一次查询获取所有已存在的记录
        List<String> langCodes = new ArrayList<>(langMap.keySet());
        List<MdmI18nEntryLang> existingLangs = mdmI18nEntryLangMapper.selectList(Wrappers.<MdmI18nEntryLang>lambdaQuery()
                .eq(MdmI18nEntryLang::getEntryId, entryId).in(MdmI18nEntryLang::getLangCode, langCodes));

        // 转换为Map便于查找
        Map<String, MdmI18nEntryLang> existingMap = existingLangs.stream()
                .collect(Collectors.toMap(MdmI18nEntryLang::getLangCode, Function.identity()));

        List<MdmI18nEntryLang> entryLangList = new ArrayList<>();

        // 分类处理插入和更新
        for (Map.Entry<String, String> entry : langMap.entrySet()) {
            String langCode = entry.getKey();
            String value = entry.getValue();

            MdmI18nEntryLang existing = existingMap.get(langCode);

            if (existing == null) {
                // 新增记录
                if (StringUtils.isNotBlank(value)) {
                    MdmI18nEntryLang newLang = new MdmI18nEntryLang();
                    newLang.setTenantId(rowDto.getTenetId());
                    newLang.setEntryId(entryId);
                    newLang.setLangCode(langCode);
                    newLang.setContent(value);
                    newLang.setCreateBy(loginUser.getUsername());
                    newLang.setCreateTime(LocalDateTime.now());
                    entryLangList.add(newLang);
                }
            } else {
                // 更新记录
                if (StringUtils.isBlank(value)) {
                    // 如果值为空，删除记录
                    mdmI18nEntryLangMapper.deleteById(existing.getId());
                } else if (!Objects.equals(existing.getContent(), value)) {
                    // 只有内容发生变化时才更新
                    existing.setContent(value);
                    existing.setCreateBy(loginUser.getUsername());
                    entryLangList.add(existing);
                }
            }
        }
        // 批量插入或更新
        if (!entryLangList.isEmpty()) {
            mdmI18nEntryLangMapper.batchInsertOrUpdate(entryLangList);
            // 同步更新到Redis
            i18nRedisSyncService.batchUpdateByEntryKey(rowDto.getEntryKey(), rowDto.getLangMap());
        }
    }

    /**
     * 国际化词条多语言翻译 导出Excel
     * @author yepeng
     * @since 2025.12.19
     */
    @Override
    public void exportExcel(HttpServletResponse response, String entryDesc, String fileName) throws IOException {
        List<MdmI18nModule> modules = mdmI18nModuleMapper.selectList(null);
        List<MdmI18nLang> langs = mdmI18nLangMapper.selectList(new QueryWrapper<MdmI18nLang>().eq("enabled", 1));
        List<MdmI18nEntry> entries;

        // 先把 entryLang 查询出来，减少循环查询
        LambdaQueryWrapper<MdmI18nEntryLang> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(entryDesc)) {
            entries = mdmI18nEntryMapper.selectList(new LambdaQueryWrapper<MdmI18nEntry>().and(q -> q.like(MdmI18nEntry::getEntryKey, entryDesc).or().like(MdmI18nEntry::getDescription, entryDesc)));
            queryWrapper.in(MdmI18nEntryLang::getEntryId, entries.stream().map(MdmI18nEntry::getId).toList());
        }else {
            entries = mdmI18nEntryMapper.selectList(null);
        }
        List<MdmI18nEntryLang> allLangs = mdmI18nEntryLangMapper.selectList(queryWrapper);
        Map<String, String> entryLangMap = allLangs.stream().collect(Collectors.toMap(e -> e.getEntryId() + "_" + e.getLangCode(), MdmI18nEntryLang::getContent, (v1, v2) -> v1));
        // 模块code->name
        Map<String, String> moduleMap = modules.stream().collect(Collectors.toMap(MdmI18nModule::getModuleCode, MdmI18nModule::getModuleName));

        // 构建表头
        List<List<String>> headList = new ArrayList<>();
        headList.add(Collections.singletonList("模块"));
        headList.add(Collections.singletonList("词条Key"));
        headList.add(Collections.singletonList("词条描述"));
        for (MdmI18nLang l : langs) {
            headList.add(Collections.singletonList(l.getLangName()+"("+ l.getLangCode() +")"));
        }

        // 构建数据行
        List<List<String>> data = new ArrayList<>();
        for (MdmI18nEntry entry : entries) {
            List<String> row = new ArrayList<>();
            row.add(moduleMap.get(entry.getModule()));
            row.add(entry.getEntryKey());
            row.add(entry.getDescription());
            for (MdmI18nLang l : langs) {
                String val = entryLangMap.getOrDefault(entry.getId() + "_" + l.getLangCode(), "");
                row.add(val);
            }
            data.add(row);
        }

        // 输出到 response
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fName);
        EasyExcelFactory.write(response.getOutputStream())
                .excelType(ExcelTypeEnum.XLSX)
                .head(headList)
                .registerWriteHandler(new I18nColumnWidthHandler())
                .sheet("i18n").doWrite(data);
    }

    /**
     * 国际化词条多语言翻译 导入Excel
     * @author yepeng
     * @since 2025.12.19
     */
    @Override
    @Transactional
    public Result<String> importExcel(MultipartFile file) throws IOException {
        try {
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            long start = System.currentTimeMillis();
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return Result.error(GlobalErrorEnum.DATA_DOES_NOT_EXIST.getCode());
            }
            // 1. 读取表头
            Row header = sheet.getRow(0);
            if (header == null) {
                return Result.error(GlobalErrorEnum.DATA_DOES_NOT_EXIST.getCode());
            }
            List<String> columns = new ArrayList<>();
            for (Cell cell : header) {
                columns.add(cell.getStringCellValue());
            }
            // 2. 语言列
            List<MdmI18nLang> langs = mdmI18nLangMapper.selectList(new QueryWrapper<MdmI18nLang>().eq("enabled", 1));
            Integer tenantId = langs.get(0).getTenantId();
            Set<String> langCodes = langs.stream().map(l->l.getLangName() +"("+ l.getLangCode() +")").collect(Collectors.toSet());

            List<I18nEntryRowDto> rows = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                I18nEntryRowDto r = new I18nEntryRowDto();
                r.setModule(getCellString(row.getCell(columns.indexOf("模块"))));
                r.setEntryKey(getCellString(row.getCell(columns.indexOf("词条Key"))));
                r.setDescription(getCellString(row.getCell(columns.indexOf("词条描述"))));
                if(StringUtils.isAnyBlank(r.getModule(), r.getEntryKey(), r.getDescription())) {
                    throw new JeecgBootBizTipException(" 第"+(i+1)+"行: 模块、词条Key、词条描述不能为空");
                }
                for (String col : columns) {
                    if (langCodes.contains(col)) {
                        r.getLangMap().put(StringUtils.substringBetween(col, "(", ")"), getCellString(row.getCell(columns.indexOf(col))));
                    }
                }
                rows.add(r);
            }
            if (rows.isEmpty()) {
                return Result.error(GlobalErrorEnum.DATA_DOES_NOT_EXIST.getCode());
            }
            // 4. 一次性查询所有 Entry
            Set<String> entryKeys = rows.stream().map(I18nEntryRowDto::getEntryKey).collect(Collectors.toSet());
            List<MdmI18nEntry> existEntries = mdmI18nEntryMapper.selectList(new QueryWrapper<MdmI18nEntry>().in("entry_key", entryKeys));
            Map<String, MdmI18nEntry> entryMap = existEntries.stream().collect(Collectors.toMap(MdmI18nEntry::getEntryKey, e -> e));
            // 5. 组装 Entry 批量数据
            List<MdmI18nEntry> entryUpdateList = new ArrayList<>();
            for (I18nEntryRowDto r : rows) {
                MdmI18nEntry entry = entryMap.get(r.getEntryKey());
                if (entry == null) {
                    entry = new MdmI18nEntry();
                    entry.setEntryKey(r.getEntryKey());
                    entry.setModule(r.getModule());
                    entry.setDescription(r.getDescription());
                    mdmI18nEntryMapper.insert(entry);
                }else{
                    entry.setDescription(r.getDescription());
                    entryUpdateList.add(entry);
                }
                entryMap.put(r.getEntryKey(), entry);
            }
            // 6. Entry 批量更新
            mdmI18nEntryMapper.batchInsertOrUpdate(entryUpdateList);
            // 确保 ID 已回填
            Set<Integer> entryIds = entryMap.values().stream().map(MdmI18nEntry::getId).collect(Collectors.toSet());
            // 7. 一次性查询所有 EntryLang
            List<MdmI18nEntryLang> existLangs = mdmI18nEntryLangMapper.selectList(new QueryWrapper<MdmI18nEntryLang>().in("entry_id", entryIds).in("lang_code", langCodes));
            Map<String, MdmI18nEntryLang> langMap = existLangs.stream().collect(Collectors.toMap(e -> e.getEntryId() + "_" + e.getLangCode(), e -> e));
            // 8. 组装 EntryLang 批量数据
            List<MdmI18nEntryLang> langSaveList = new ArrayList<>();
            for (I18nEntryRowDto r : rows) {
                MdmI18nEntry entry = entryMap.get(r.getEntryKey());
                for (Map.Entry<String, String> lv : r.getLangMap().entrySet()) {
                    String key = entry.getId() + "_" + lv.getKey();
                    MdmI18nEntryLang el = langMap.get(key);
                    if (el == null) {
                        el = new MdmI18nEntryLang();
                        el.setTenantId(tenantId);
                        el.setEntryId(entry.getId());
                        el.setEntryKey(entry.getEntryKey());
                        el.setLangCode(lv.getKey());
                        el.setCreateBy(loginUser.getUsername());
                        el.setCreateTime(LocalDateTime.now());
                    }
                    el.setContent(lv.getValue());
                    langSaveList.add(el);
                }
            }
            // 9. EntryLang 批量插入或更新
            if (!langSaveList.isEmpty()) {
                mdmI18nEntryLangMapper.batchInsertOrUpdate(langSaveList);
                //更新各语言Redis缓存并升级语言版本
                Map<String, Map<String, String>> langEntryMap = langSaveList.stream().collect(Collectors.groupingBy(MdmI18nEntryLang::getLangCode,
                    Collectors.toMap(MdmI18nEntryLang::getEntryKey, MdmI18nEntryLang::getContent, (v1, v2) -> v2)
                ));
                langEntryMap.keySet().forEach(langCode -> i18nRedisSyncService.batchUpdateLangCode(langCode, langEntryMap.get(langCode)));
            }
            workbook.close();
            log.info("消耗时间" + (System.currentTimeMillis() - start) + "毫秒");
            return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            if(msg != null && msg.contains("Duplicate entry")){
                return Result.error("文件导入失败:有重复数据！");
            }else{
                return Result.error(GlobalErrorEnum.OPERATION_FAILED.getCode() + ":" + e.getMessage());
            }
        }  finally {
            try {
                file.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取单元格内容
     * @param cell 单元格对象
     */
    private String getCellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

}