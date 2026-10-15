package com.keytech.modules.manage.check.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.manage.check.entity.ManagerCheckConfig;
import com.keytech.modules.manage.check.mapper.ManagerCheckConfigMapper;
import com.keytech.modules.manage.check.service.IManagerCheckConfigService;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.entity.MdmPost;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.mdm3.service.IMdmPostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.MapExcelConstants;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.params.ExcelExportEntity;
import org.jeecgframework.poi.excel.view.JeecgMapExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 分层检查-检查识别矩阵
 * @Author: jeecg-boot
 * @Date:   2025-04-08
 * @Version: V1.0
 */
@Service
public class ManagerCheckConfigServiceImpl extends BaseServiceImpl<ManagerCheckConfigMapper, ManagerCheckConfig> implements IManagerCheckConfigService {
    @Autowired
    private IMdmPostService iMdmPostService;

    @Autowired
    private IMdmDictService mdmDictService;

    @Autowired
    private ISysDepartService sysDepartService;

    @Override
    public ModelAndView exportXls(HttpServletRequest request, ManagerCheckConfig managerCheckConfig, String title) {
        //组装查询条件
        QueryWrapper<ManagerCheckConfig> queryWrapper = QueryGenerator.initQueryWrapper(managerCheckConfig, request.getParameterMap());
        String selections = request.getParameter("selections");
        List<String> exportList;
        if (oConvertUtils.isNotEmpty(selections)) {
            exportList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", exportList);
        }
        //获取数据
        List<Map<String, Object>> mapList = this.listMaps(queryWrapper);

        //获取各项辅助数据map
        Map<String, Object> auxiliaryMap = getAuxiliaryData(managerCheckConfig.getDeptCode(), null);
        Map<String, SysDepart> allDeptMap = (Map<String, SysDepart>) auxiliaryMap.get("allDeptMap");
        Map<String, MdmDict> dictMap = (Map<String, MdmDict>) auxiliaryMap.get("dictMap");
        List<MdmPost> postList = (List<MdmPost>) auxiliaryMap.get("postList");
        //处理数据
        for(Map<String, Object> map : mapList){
            map.put("factory_code", allDeptMap.get(map.get("factory_code")).getDepartName());
            map.put("dept_code", allDeptMap.get(map.get("dept_code")).getDepartName());
            if(null != map.get("line_code")){
                map.put("line_code", allDeptMap.get(map.get("line_code")).getDepartName());
            }
            if(null != map.get("jcxly_dm") && dictMap.containsKey("MES3_MANAGE_CHECK_SOURCE-"+map.get("jcxly_dm"))){
                map.put("jcxly_dm", dictMap.get("MES3_MANAGE_CHECK_SOURCE-"+map.get("jcxly_dm")).getItemKey());
            }
            if(null != map.get("jctx_dm") && dictMap.containsKey("MES3_MANAGE_CHECK_TX-"+map.get("jctx_dm"))){
                map.put("jctx_dm", dictMap.get("MES3_MANAGE_CHECK_TX-"+map.get("jctx_dm")).getItemKey());
            }
            if(StrUtil.isNotBlank(MapUtil.getStr(map, "if_zx"))){
                map.put("if_zx", "0".equals(MapUtil.getStr(map, "if_zx")) ? "否" : "是");
            }

            if(map.containsKey("gwpz_json")){
                JSONArray jsonArray = JSONUtil.parseArray(map.get("gwpz_json"));
                jsonArray.forEach(obj -> {
                    JSONObject jsonObject = (JSONObject) obj;
                    String text = "频率："+jsonObject.getStr("plName")+"，周期："+jsonObject.getStr("weekName");
                    map.put(jsonObject.getStr("id"), text);
                });
            }
        }

        //获取当前用户
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //组装表头
        List<ExcelExportEntity> beanList = new ArrayList<>();
        beanList.add(new ExcelExportEntity("ID", "id", 20));
        beanList.add(new ExcelExportEntity("工厂", "factory_code", 15));
        beanList.add(new ExcelExportEntity("部门", "dept_code", 15));
        beanList.add(new ExcelExportEntity("产线", "line_code", 15));
        beanList.add(new ExcelExportEntity("检查项编号", "check_bh", 15));
        beanList.add(new ExcelExportEntity("常规活动", "cghd", 15));
        beanList.add(new ExcelExportEntity("检查项来源", "jcxly_dm", 15));
        beanList.add(new ExcelExportEntity("检查体系", "jctx_dm", 15));
        beanList.add(new ExcelExportEntity("检查内容", "jcnr", 15));
        beanList.add(new ExcelExportEntity("是否必须在生产下执行", "if_zx", 15));
        beanList.add(new ExcelExportEntity("排序", "sort", 15));
        for(MdmPost mdmPost : postList){
            beanList.add(new ExcelExportEntity(mdmPost.getPostName(), mdmPost.getId().toString(), 20));

            //循环处理没配置的岗位添加默认斜杆
            for(Map<String, Object> map : mapList){
                if(!map.containsKey(mdmPost.getId().toString())){
                    map.put(mdmPost.getId().toString(), "/");
                }
            }
        }

        ModelAndView mv = new ModelAndView(new JeecgMapExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, title);
        mv.addObject(MapExcelConstants.ENTITY_LIST, beanList);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams(title, "导出人:" + sysUser.getRealname(), title));
        mv.addObject(NormalExcelConstants.MAP_LIST, mapList );
        return mv;
    }

    @Override
    @Transactional
    public Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //获取导入文件
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

        //错误信息集合
        List<String> errorMessage = new ArrayList<>();
        //成功数，错误数
        int successLines = 0, errorLines = 0;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(2);
            params.setNeedSave(true);
            try {
                //获取上传文件中的数据-map对象，未转义,用于获取动态表头部分的数据
                List<Map<String, Object>> mapList = ExcelImportUtil.importExcel(file.getInputStream(), Map.class, params);

                //获取各项辅助数据map
                Map<String, Object> auxiliaryMap = getAuxiliaryData(null, mapList.get(0).get("部门").toString());
                List<MdmPost> postList = (List<MdmPost>) auxiliaryMap.get("postList");
                Map<String, MdmDict> dictNameMap = (Map<String, MdmDict>) auxiliaryMap.get("dictNameMap");

                //操作数据
                List<ManagerCheckConfig> saveList = new ArrayList<>();
                int lineNumber = 3;
                for(Map<String, Object> map : mapList){
                    lineNumber++;
                    ManagerCheckConfig managerCheckConfig = new ManagerCheckConfig();
                    String factory = valideDate(errorMessage, "工厂", auxiliaryMap, map, 1, lineNumber, true, "");
                    if(null == factory){
                        errorLines++;
                        continue;
                    } else {
                        managerCheckConfig.setFactoryCode(factory);
                    }

                    String dept = valideDate(errorMessage, "部门", auxiliaryMap, map, 1, lineNumber, true, "");
                    if(null == dept){
                        errorLines++;
                        continue;
                    } else {
                        managerCheckConfig.setDeptCode(dept);
                    }

                    String lineCode = valideDate(errorMessage, "产线", auxiliaryMap, map, 1, lineNumber, true, "");
                    if(null != lineCode){
                        managerCheckConfig.setLineCode(lineCode);
                    }

                    String jcxbh = valideDate(errorMessage, "检查项编号", auxiliaryMap, map, 1, lineNumber, false, "");
                    if(null == jcxbh){
                        errorLines++;
                        continue;
                    } else {
                        managerCheckConfig.setCheckBh(jcxbh);
                    }

                    String cghd = valideDate(errorMessage, "常规活动", auxiliaryMap, map, 1, lineNumber, false, "");
                    if(null == cghd){
                        errorLines++;
                        continue;
                    }else{
                        managerCheckConfig.setCghd(cghd);
                    }

                    String ly = valideDate(errorMessage, "检查项来源", auxiliaryMap, map, 2, lineNumber, true, "MES3_MANAGE_CHECK_SOURCE-");
                    if(null == ly){
                        errorLines++;
                        continue;
                    }else{
                        managerCheckConfig.setJcxlyDm(ly);
                    }

                    String tx = valideDate(errorMessage, "检查体系", auxiliaryMap, map, 2, lineNumber, true, "MES3_MANAGE_CHECK_TX-");
                    if(null == tx){
                        errorLines++;
                        continue;
                    }else{
                        managerCheckConfig.setJctxDm(tx);
                    }

                    String jcnr = valideDate(errorMessage, "检查内容", auxiliaryMap, map, 1, lineNumber, false, "");
                    if(null == jcnr){
                        errorLines++;
                        continue;
                    }else{
                        managerCheckConfig.setJcnr(jcnr);
                    }

                    String ifZx = valideDate(errorMessage, "是否必须在生产下执行", auxiliaryMap, map, 1, lineNumber, false, "");
                    if(null == ifZx){
                        errorLines++;
                        continue;
                    }else{
                        managerCheckConfig.setIfZx("是".equals(ifZx) ? 1 : 0);
                    }

                    boolean flag = false;
                    JSONArray gwpzArray = new JSONArray();
                    //解析岗位配置json数据
                    for(MdmPost post: postList){
                        //如果不为空且不等于/则继续往下处理
                        if(StrUtil.isNotBlank(MapUtil.getStr(map, post.getPostName())) && !"/".equals(MapUtil.getStr(map, post.getPostName()))){
                            JSONObject jsonObject = new JSONObject();
                            //频率和周期用中文逗号(，)隔开，如果长度不是2则格式不正确
                            String[] arr = MapUtil.getStr(map, post.getPostName()).split("，");
                            if(arr.length != 2){
                                errorMessage.add("第" + lineNumber + "行，"+post.getPostName()+"：数据格式不正确");
                                errorLines++;
                                flag = true;
                                break;
                            }
                            //频率用中文的冒号(：)隔开，如果长度不是2则格式不正确
                            String[] plArr = arr[0].split("：");
                            if(plArr.length != 2){
                                errorMessage.add("第" + lineNumber + "行，"+post.getPostName()+"：频率格式不正确");
                                errorLines++;
                                flag = true;
                                break;
                            }

                            String pl;
                            String plName;
                            //如果字典中没有对应频率
                            if(!dictNameMap.containsKey("MES3_MANAGE_CHECK_PL-"+plArr[1])){
                                errorMessage.add("第" + lineNumber + "行，"+post.getPostName()+"：频率不存在");
                                errorLines++;
                                flag = true;
                                break;
                            }else{
                                //频率正确的话处理周期
                                pl = dictNameMap.get("MES3_MANAGE_CHECK_PL-"+plArr[1]).getItemVal();
                                plName = plArr[1];
                                //周期用中文的冒号(：)隔开，如果长度不是2则格式不正确
                                String[] cycleArr = arr[1].split("：");
                                if(cycleArr.length != 2){
                                    errorMessage.add("第" + lineNumber + "行，"+post.getPostName()+"：周期格式不正确");
                                    errorLines++;
                                    flag = true;
                                    break;
                                }
                                //如果频率值小于3，周期值用英文的逗号(,)隔开，如果长度不是和频率不一致则格式不正确
                                String[] weekNameArr = cycleArr[1].split(",");
                                if(Integer.parseInt(pl) < 3 && weekNameArr.length != Integer.parseInt(pl)){
                                    errorMessage.add("第" + lineNumber + "行，"+post.getPostName()+"：周期格式与频率不符合");
                                    errorLines++;
                                    flag = true;
                                    break;
                                }

                                String[] weekArr = new String[2];
                                for(int i = 0; i < weekNameArr.length; i++){
                                    //判断是否存在于字典中
                                    if(!dictNameMap.containsKey("MES3_MANAGE_CHECK_WEEK-"+weekNameArr[i])){
                                        errorMessage.add("第" + lineNumber + "行，"+post.getPostName()+"：周期不存在");
                                        errorLines++;
                                        flag = true;
                                        break;
                                    }
                                    weekArr[i] = dictNameMap.get("MES3_MANAGE_CHECK_WEEK-"+weekNameArr[i]).getItemVal();
                                }
                                jsonObject.set("week", String.join(",", weekArr));
                                jsonObject.set("weekName", String.join(",", weekNameArr));
                            }
                            jsonObject.set("id", post.getId());
                            jsonObject.set("name", post.getPostName());
                            jsonObject.set("pl", pl);
                            jsonObject.set("plName", plName);
                            gwpzArray.add(jsonObject);
                        }
                    }
                    if(flag){
                        continue;
                    }

                    managerCheckConfig.setId(MapUtil.getStr(map, "ID"));
                    managerCheckConfig.setSort(MapUtil.getDouble(map, "排序"));
                    managerCheckConfig.setGwpzJson(JSONUtil.toJsonStr(gwpzArray));
                    saveList.add(managerCheckConfig);
                    successLines++;
                }
                this.saveOrUpdateBatch(saveList);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                errorLines++;
                errorMessage.add("发生异常：" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
    }

    /**
     * 校验数据
     * @param errorMessage 错误集合
     * @param key 字段key
     * @param auxiliaryMap  所有辅助数据map
     * @param dataMap 需校验的数据map
     * @param type  字典类型1：部门；2：字典；3：岗位
     * @param lineNumber 当前行数
     * @param isValid 是否需要字典校验
     * @param dictPrefix 字典前缀
     * @return 结果
     */
    private String valideDate(List<String> errorMessage, String key, Map<String, Object> auxiliaryMap,
                               Map<String, Object> dataMap, int type, int lineNumber, boolean isValid, String dictPrefix){
        Map<String, SysDepart> allDeptNameMap = (Map<String, SysDepart>) auxiliaryMap.get("allDeptNameMap");
        Map<String, MdmDict> dictNameMap = (Map<String, MdmDict>) auxiliaryMap.get("dictNameMap");
        String result = null;
        Map<String, ?> validMap = dictNameMap;
        if(type == 1){
            validMap = allDeptNameMap;
        }

        if(!dataMap.containsKey(key)
            || StrUtil.isBlank(MapUtil.getStr(dataMap, key))
            || (isValid && !validMap.containsKey(dictPrefix+MapUtil.getStr(dataMap, key)))
        ){
            errorMessage.add("第" + lineNumber + "行，"+key+"不存在");
        }else{
            if(isValid){
                if(type == 1){
                    result = allDeptNameMap.get(MapUtil.getStr(dataMap, key)).getOrgCode();
                } else if(type == 2){
                    result = dictNameMap.get(dictPrefix+MapUtil.getStr(dataMap, key)).getItemVal();
                }
            } else {
                result = MapUtil.getStr(dataMap, key);
            }
        }
        return result;
    }

    /**
     * 获取各种辅助数据
     * @return 结果
     */
    private Map<String, Object> getAuxiliaryData(String deptCode, String deptName){
        //所有单位信息
        List<SysDepart> allDeptList = sysDepartService.list();
        //编码为key的map
        Map<String, SysDepart> allDeptMap = CollStreamUtil.toMap(allDeptList, SysDepart::getOrgCode, bean -> bean);
        //名称为key的map
        Map<String, SysDepart> allDeptNameMap = CollStreamUtil.toMap(allDeptList, SysDepart::getDepartName, bean -> bean);

        if(null == deptCode && StrUtil.isNotBlank(deptName)){
            deptCode = allDeptNameMap.get(deptName).getOrgCode();
        }

        //获取所有管理岗岗位
        List<MdmPost> postList = iMdmPostService.list(
                new LambdaQueryWrapper<MdmPost>()
                        .eq(MdmPost::getDeptCode, deptCode)
                        .eq(MdmPost::getPostType, "1")
        );
        //名称为key的map
        Map<String, MdmPost> postNameMap = CollStreamUtil.toMap(postList, MdmPost::getPostName, bean -> bean);

        //获取来源、体系字典
        List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
                List.of(
                        "MES3_MANAGE_CHECK_SOURCE"
                        , "MES3_MANAGE_CHECK_TX"
                        , "MES3_MANAGE_CHECK_WEEK"
                        , "MES3_MANAGE_CHECK_PL"
                )
        );
        //字典数据map集合，key为groupCode-itemVal,value为对象信息
        Map<String, MdmDict> dictMap = mdmDictList.stream()
                .collect(Collectors.toMap(
                        item -> item.getGroupCode() + "-" + item.getItemVal(),
                        item -> item
                ));
        //字典数据map集合，key为groupCode-itemKey,value为对象信息
        Map<String, MdmDict> dictNameMap = mdmDictList.stream()
                .collect(Collectors.toMap(
                        item -> item.getGroupCode() + "-" + item.getItemKey(),
                        item -> item
                ));
        //返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("postList", postList);
        result.put("postNameMap", postNameMap);
        result.put("allDeptMap", allDeptMap);
        result.put("allDeptNameMap", allDeptNameMap);
        result.put("dictMap", dictMap);
        result.put("dictNameMap", dictNameMap);
        return result;
    }
}
