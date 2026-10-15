package com.keytech.modules.pi.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.entity.PiAbnormalAnalysisPoint;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.mapper.PiAbnormalAnalysisPointMapper;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisPointService;
import com.keytech.modules.pi.service.IPiCustomizeTableFieldService;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;

/**
 * @Description: 异常点位配置
 * @Author: jeecg-boot
 * @Date:   2025-03-04
 * @Version: V1.0
 */
@Service
public class PiAbnormalAnalysisPointServiceImpl extends BaseServiceImpl<PiAbnormalAnalysisPointMapper, PiAbnormalAnalysisPoint> implements IPiAbnormalAnalysisPointService {
    @Autowired
    private ISysDepartService sysDepartService;

    @Autowired
    private IPiCustomizeTableService piCustomizeTableService;

    @Autowired
    private IPiCustomizeTableFieldService piCustomizeTableFieldService;
	@Override
	public void addPiAbnormalAnalysisPoint(PiAbnormalAnalysisPoint piAbnormalAnalysisPoint) {
	   //新增时设置hasChild为0
	    piAbnormalAnalysisPoint.setHasChild(IPiAbnormalAnalysisPointService.NOCHILD);
		if(oConvertUtils.isEmpty(piAbnormalAnalysisPoint.getParentId())){
			piAbnormalAnalysisPoint.setParentId(IPiAbnormalAnalysisPointService.ROOT_PID_VALUE);
		}else{
			//如果当前节点父ID不为空 则设置父节点的hasChildren 为1
			PiAbnormalAnalysisPoint parent = baseMapper.selectById(piAbnormalAnalysisPoint.getParentId());
			if(parent!=null && !"1".equals(parent.getHasChild())){
				parent.setHasChild("1");
				baseMapper.updateById(parent);
			}
		}
		baseMapper.insert(piAbnormalAnalysisPoint);
	}
	
	@Override
	public void updatePiAbnormalAnalysisPoint(PiAbnormalAnalysisPoint piAbnormalAnalysisPoint) {
		PiAbnormalAnalysisPoint entity = this.getById(piAbnormalAnalysisPoint.getId());
		if(entity==null) {
			throw new JeecgBootException("未找到对应实体");
		}
		String old_pid = entity.getParentId();
		String new_pid = piAbnormalAnalysisPoint.getParentId();
		if(!old_pid.equals(new_pid)) {
			updateOldParentNode(old_pid);
			if(oConvertUtils.isEmpty(new_pid)){
				piAbnormalAnalysisPoint.setParentId(IPiAbnormalAnalysisPointService.ROOT_PID_VALUE);
			}
			if(!IPiAbnormalAnalysisPointService.ROOT_PID_VALUE.equals(piAbnormalAnalysisPoint.getParentId())) {
				baseMapper.updateTreeNodeStatus(piAbnormalAnalysisPoint.getParentId(), IPiAbnormalAnalysisPointService.HASCHILD);
			}
		}
		baseMapper.updateById(piAbnormalAnalysisPoint);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deletePiAbnormalAnalysisPoint(String id) throws JeecgBootException {
		//查询选中节点下所有子节点一并删除
        id = this.queryTreeChildIds(id);
        if(id.indexOf(",")>0) {
            StringBuffer sb = new StringBuffer();
            String[] idArr = id.split(",");
            for (String idVal : idArr) {
                if(idVal != null){
                    PiAbnormalAnalysisPoint piAbnormalAnalysisPoint = this.getById(idVal);
                    String pidVal = piAbnormalAnalysisPoint.getParentId();
                    //查询此节点上一级是否还有其他子节点
                    List<PiAbnormalAnalysisPoint> dataList = baseMapper.selectList(new QueryWrapper<PiAbnormalAnalysisPoint>().eq("parent_id", pidVal).notIn("id",Arrays.asList(idArr)));
                    boolean flag = (dataList == null || dataList.size() == 0) && !Arrays.asList(idArr).contains(pidVal) && !sb.toString().contains(pidVal);
                    if(flag){
                        //如果当前节点原本有子节点 现在木有了，更新状态
                        sb.append(pidVal).append(",");
                    }
                }
            }
            //批量删除节点
            baseMapper.deleteBatchIds(Arrays.asList(idArr));
            //修改已无子节点的标识
            String[] pidArr = sb.toString().split(",");
            for(String pid : pidArr){
                this.updateOldParentNode(pid);
            }
        }else{
            PiAbnormalAnalysisPoint piAbnormalAnalysisPoint = this.getById(id);
            if(piAbnormalAnalysisPoint==null) {
                throw new JeecgBootException("未找到对应实体");
            }
            updateOldParentNode(piAbnormalAnalysisPoint.getParentId());
            baseMapper.deleteById(id);
        }
	}
	
	@Override
    public List<PiAbnormalAnalysisPoint> queryTreeListNoPage(QueryWrapper<PiAbnormalAnalysisPoint> queryWrapper) {
        List<PiAbnormalAnalysisPoint> dataList = baseMapper.selectList(queryWrapper);
        List<PiAbnormalAnalysisPoint> mapList = new ArrayList<>();
        for(PiAbnormalAnalysisPoint data : dataList){
            String pidVal = data.getParentId();
            //递归查询子节点的根节点
            if(pidVal != null && !IPiAbnormalAnalysisPointService.NOCHILD.equals(pidVal)){
                PiAbnormalAnalysisPoint rootVal = this.getTreeRoot(pidVal);
                if(rootVal != null && !mapList.contains(rootVal)){
                    mapList.add(rootVal);
                }
            }else{
                if(!mapList.contains(data)){
                    mapList.add(data);
                }
            }
        }
        return mapList;
    }

    @Override
    public List<SelectTreeModel> queryListByCode(String parentCode) {
        String pid = ROOT_PID_VALUE;
        if (oConvertUtils.isNotEmpty(parentCode)) {
            LambdaQueryWrapper<PiAbnormalAnalysisPoint> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PiAbnormalAnalysisPoint::getParentId, parentCode);
            List<PiAbnormalAnalysisPoint> list = baseMapper.selectList(queryWrapper);
            if (list == null || list.size() == 0) {
                throw new JeecgBootException("该编码【" + parentCode + "】不存在，请核实!");
            }
            if (list.size() > 1) {
                throw new JeecgBootException("该编码【" + parentCode + "】存在多个，请核实!");
            }
            pid = list.get(0).getId();
        }
        return baseMapper.queryListByPid(pid, null);
    }

    @Override
    public List<SelectTreeModel> queryListByPid(String pid) {
        if (oConvertUtils.isEmpty(pid)) {
            pid = ROOT_PID_VALUE;
        }
        return baseMapper.queryListByPid(pid, null);
    }

    @Override
    public Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<PiAbnormalAnalysisPoint> clazz) throws IOException {
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
                //所有单位信息
                List<SysDepart> allDeptList = sysDepartService.list();
                Map<String, SysDepart> allDeptMap = CollStreamUtil.toMap(allDeptList, SysDepart::getOrgCode, bean -> bean);

                //获取上传文件中的数据
                List<PiAbnormalAnalysisPoint> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);
                //获取报表编码
                String tableCode = list.get(0).getTableCode();
                List<PiCustomizeTable> tableList = piCustomizeTableService.list(new LambdaQueryWrapper<PiCustomizeTable>().eq(PiCustomizeTable::getTableName, tableCode));
                if(CollUtil.isEmpty(tableList)){
                    errorMessage.add("报表不存在");
                    return ImportExcelUtil.imporReturnRes(1, 0, errorMessage);
                }
                String tableText = tableList.get(0).getName();
                //获取PI基础表数据
                LambdaQueryWrapper<PiCustomizeTableField> fieldQueryWrapper = new LambdaQueryWrapper<>();
                fieldQueryWrapper.eq(PiCustomizeTableField::getTableName, tableCode);
                List<PiCustomizeTableField> fieldList = piCustomizeTableFieldService.list(fieldQueryWrapper);
                Map<String, PiCustomizeTableField> fieldMap = CollStreamUtil.toMap(fieldList, PiCustomizeTableField::getFieldName, bean -> bean);

                //操作数据
                List<PiAbnormalAnalysisPoint> saveList = new ArrayList<>();
                int lineNumber = 3;
                for(PiAbnormalAnalysisPoint piAbnormalAnalysisPoint : list){
                    lineNumber++;
                    if(StrUtil.isBlank(piAbnormalAnalysisPoint.getFactoryCode()) || !allDeptMap.containsKey(piAbnormalAnalysisPoint.getFactoryCode())){
                        errorLines++;
                        errorMessage.add("第 " + lineNumber + "行， 工厂不存在。");
                        continue;
                    }

                    if(StrUtil.isBlank(piAbnormalAnalysisPoint.getDeptCode()) || !allDeptMap.containsKey(piAbnormalAnalysisPoint.getDeptCode())){
                        errorLines++;
                        errorMessage.add("第 " + lineNumber + "行， 部门不存在。");
                        continue;
                    }

                    if(StrUtil.isBlank(piAbnormalAnalysisPoint.getLineCode()) || !allDeptMap.containsKey(piAbnormalAnalysisPoint.getLineCode())){
                        errorLines++;
                        errorMessage.add("第 " + lineNumber + "行， 产线不存在。");
                        continue;
                    }

                    if(StrUtil.isBlank(piAbnormalAnalysisPoint.getFieldCode()) || !fieldMap.containsKey(piAbnormalAnalysisPoint.getFieldCode())){
                        errorLines++;
                        errorMessage.add("第 " + lineNumber + "行， pi编码不存在。");
                        continue;
                    }
                    successLines++;

                    if(null == piAbnormalAnalysisPoint.getParentId()){
                        piAbnormalAnalysisPoint.setParentId("0");
                    }
                    piAbnormalAnalysisPoint.setTableName(tableText);
                    piAbnormalAnalysisPoint.setFieldName(fieldMap.get(piAbnormalAnalysisPoint.getFieldCode()).getName());
                    saveList.add(piAbnormalAnalysisPoint);
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
                    e.printStackTrace();
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
    }

    /**
	 * 根据所传pid查询旧的父级节点的子节点并修改相应状态值
	 * @param pid
	 */
	private void updateOldParentNode(String pid) {
		if(!IPiAbnormalAnalysisPointService.ROOT_PID_VALUE.equals(pid)) {
			Long count = baseMapper.selectCount(new QueryWrapper<PiAbnormalAnalysisPoint>().eq("parent_id", pid));
			if(count==null || count<=1) {
				baseMapper.updateTreeNodeStatus(pid, IPiAbnormalAnalysisPointService.NOCHILD);
			}
		}
	}

	/**
     * 递归查询节点的根节点
     * @param pidVal
     * @return
     */
    private PiAbnormalAnalysisPoint getTreeRoot(String pidVal){
        PiAbnormalAnalysisPoint data =  baseMapper.selectById(pidVal);
        if(data != null && !IPiAbnormalAnalysisPointService.ROOT_PID_VALUE.equals(data.getParentId())){
            return this.getTreeRoot(data.getParentId());
        }else{
            return data;
        }
    }

    /**
     * 根据id查询所有子节点id
     * @param ids
     * @return
     */
    private String queryTreeChildIds(String ids) {
        //获取id数组
        String[] idArr = ids.split(",");
        StringBuffer sb = new StringBuffer();
        for (String pidVal : idArr) {
            if(pidVal != null){
                if(!sb.toString().contains(pidVal)){
                    if(sb.toString().length() > 0){
                        sb.append(",");
                    }
                    sb.append(pidVal);
                    this.getTreeChildIds(pidVal,sb);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 递归查询所有子节点
     * @param pidVal
     * @param sb
     * @return
     */
    private StringBuffer getTreeChildIds(String pidVal,StringBuffer sb){
        List<PiAbnormalAnalysisPoint> dataList = baseMapper.selectList(new QueryWrapper<PiAbnormalAnalysisPoint>().eq("parent_id", pidVal));
        if(dataList != null && dataList.size()>0){
            for(PiAbnormalAnalysisPoint tree : dataList) {
                if(!sb.toString().contains(tree.getId())){
                    sb.append(",").append(tree.getId());
                }
                this.getTreeChildIds(tree.getId(),sb);
            }
        }
        return sb;
    }

}
