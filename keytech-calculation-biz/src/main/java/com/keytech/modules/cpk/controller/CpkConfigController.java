package com.keytech.modules.cpk.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.cpk.entity.CpkConfig;
import com.keytech.modules.cpk.entity.CpkConfigDetail;
import com.keytech.modules.cpk.service.ICpkConfigDetailService;
import com.keytech.modules.cpk.service.ICpkConfigService;
import com.keytech.modules.cpk.util.CpkUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

 /**
 * @Description: CPK配置
 * @Author: jeecg-boot
 * @Date:   2025-12-11
 * @Version: V1.0
 */
@Tag(name="CPK配置")
@RestController
@RequestMapping("/cpk/cpkConfig")
@Slf4j
public class CpkConfigController extends JeecgController<CpkConfig, ICpkConfigService>{
	@Autowired
	private ICpkConfigService cpkConfigService;
	 @Resource
	 private ICpkConfigDetailService cpkConfigDetailService;

	/**
	 * 分页列表查询
	 *
	 * @param cpkConfig
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="CPK配置-分页列表查询")
	@GetMapping(value = "/rootList")
	public Result<IPage<CpkConfig>> queryPageList(CpkConfig cpkConfig,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CpkConfig> queryWrapper = QueryGenerator.initQueryWrapper(cpkConfig, req.getParameterMap());
		String parentIds = req.getParameter("parentIds");
		if(StrUtil.isNotBlank(parentIds)){
			List<String> parentIdList = Arrays.asList(parentIds.split(","));
			queryWrapper.in("parent_id", parentIdList);
		}
		Page<CpkConfig> page = new Page<>(pageNo, pageSize);
		IPage<CpkConfig> pageList = cpkConfigService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	 /**
	  * 【vue3专用】加载节点的子数据
	  *
	  * @param pid
	  * @return
	  */
	 @RequestMapping(value = "/loadTreeChildren", method = RequestMethod.GET)
	 public Result<List<SelectTreeModel>> loadTreeChildren(@RequestParam(name = "pid") String pid) {
		 Result<List<SelectTreeModel>> result = new Result<>();
		 try {
			 List<SelectTreeModel> ls = cpkConfigService.queryListByPid(pid);
			 result.setResult(ls);
			 result.setSuccess(true);
		 } catch (Exception e) {
			 e.printStackTrace();
			 result.setMessage(e.getMessage());
			 result.setSuccess(false);
		 }
		 return result;
	 }

	 /**
	  * 【vue3专用】加载一级节点/如果是同步 则所有数据
	  *
	  * @param async
	  * @param pcode
	  * @return
	  */
	 @RequestMapping(value = "/loadTreeRoot", method = RequestMethod.GET)
	 public Result<List<SelectTreeModel>> loadTreeRoot(@RequestParam(name = "async") Boolean async, @RequestParam(name = "pcode") String pcode) {
		 Result<List<SelectTreeModel>> result = new Result<>();
		 try {
			 List<SelectTreeModel> ls = cpkConfigService.queryListByCode(pcode);
			 if (!async) {
				 loadAllChildren(ls);
			 }
			 result.setResult(ls);
			 result.setSuccess(true);
		 } catch (Exception e) {
			 e.printStackTrace();
			 result.setMessage(e.getMessage());
			 result.setSuccess(false);
		 }
		 return result;
	 }

	 /**
	  * 【vue3专用】递归求子节点 同步加载用到
	  *
	  * @param ls
	  */
	 private void loadAllChildren(List<SelectTreeModel> ls) {
		 for (SelectTreeModel tsm : ls) {
			 List<SelectTreeModel> temp = cpkConfigService.queryListByPid(tsm.getKey());
			 if (temp != null && temp.size() > 0) {
				 tsm.setChildren(temp);
				 loadAllChildren(temp);
			 }
		 }
	 }

	 /**
      * 获取子数据
      * @param cpkConfig
      * @param req
      * @return
      */
	//@AutoLog(value = "CPK配置-获取子数据")
	@Operation(summary="CPK配置-获取子数据")
	@GetMapping(value = "/childList")
	public Result<IPage<CpkConfig>> queryPageList(CpkConfig cpkConfig,HttpServletRequest req) {
		QueryWrapper<CpkConfig> queryWrapper = QueryGenerator.initQueryWrapper(cpkConfig, req.getParameterMap());
		List<CpkConfig> list = cpkConfigService.list(queryWrapper);
		IPage<CpkConfig> pageList = new Page<>(1, 10, list.size());
        pageList.setRecords(list);
		return Result.OK(pageList);
	}

    /**
      * 批量查询子节点
      * @param parentIds 父ID（多个采用半角逗号分割）
      * @return 返回 IPage
      * @param parentIds
      * @return
      */
	//@AutoLog(value = "CPK配置-批量获取子数据")
    @Operation(summary="CPK配置-批量获取子数据")
    @GetMapping("/getChildListBatch")
    public Result getChildListBatch(@RequestParam("parentIds") String parentIds) {
        try {
            QueryWrapper<CpkConfig> queryWrapper = new QueryWrapper<>();
            List<String> parentIdList = Arrays.asList(parentIds.split(","));
            queryWrapper.in("parent_id", parentIdList);
            List<CpkConfig> list = cpkConfigService.list(queryWrapper);
            IPage<CpkConfig> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("批量查询子节点失败：" + e.getMessage());
        }
    }
	
	/**
	 *   添加
	 *
	 * @param cpkConfig
	 * @return
	 */
	@AutoLog(value = "CPK配置-添加")
	@Operation(summary="CPK配置-添加")
    @RequiresPermissions("cpk:mes3_cpk_config:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CpkConfig cpkConfig) {
		cpkConfigService.addCpkConfig(cpkConfig);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param cpkConfig
	 * @return
	 */
	@AutoLog(value = "CPK配置-编辑")
	@Operation(summary="CPK配置-编辑")
    @RequiresPermissions("cpk:mes3_cpk_config:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CpkConfig cpkConfig) {
		cpkConfigService.updateCpkConfig(cpkConfig);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "CPK配置-通过id删除")
	@Operation(summary="CPK配置-通过id删除")
    @RequiresPermissions("cpk:mes3_cpk_config:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cpkConfigService.deleteCpkConfig(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "CPK配置-批量删除")
	@Operation(summary="CPK配置-批量删除")
    @RequiresPermissions("cpk:mes3_cpk_config:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cpkConfigService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="CPK配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CpkConfig> queryById(@RequestParam(name="id",required=true) String id) {
		CpkConfig cpkConfig = cpkConfigService.getById(id);
		if(cpkConfig==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		cpkConfig.setDetailList(cpkConfigDetailService.lambdaQuery()
				.eq(CpkConfigDetail::getConfigId, id)
				.list());
		return Result.OK(cpkConfig);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cpkConfig
    */
    @RequiresPermissions("cpk:mes3_cpk_config:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CpkConfig cpkConfig) {
		return super.exportXls(request, cpkConfig, CpkConfig.class, "CPK配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("cpk:mes3_cpk_config:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return super.importExcel(request, response, CpkConfig.class);
    }

	 /**
	  * 获取所有数据转换成树结构
	  * @return 结果
	  */
	 @Operation(summary="CPK配置-分页列表查询")
	 @GetMapping(value = "/treeList")
	 public Result<List<CpkConfig>> queryTreeList() {
		 List<CpkConfig> list = cpkConfigService.lambdaQuery().list();
		 return Result.OK(CpkUtil.getTreeList(list, "0", 1));
	 }

	 /**
	  *   添加
	  * @param cpkConfig
	  * @return
	  */
	 @AutoLog(value = "CPK配置-添加")
	 @Operation(summary="CPK配置-添加")
	 @RequiresPermissions("cpk:mes3_cpk_config:add")
	 @PostMapping(value = "/saveDetail")
	 public Result<String> saveDetail(@RequestBody CpkConfig cpkConfig) {
		 cpkConfigService.saveDetail(cpkConfig);
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }

	 /**
	  * 获取所有数据
	  */
	 @Operation(summary="CPK配置-所有数据查询")
	 @GetMapping(value = "/allList")
	 public Result<List<CpkConfig>> queryAllList(CpkConfig cpkConfig, HttpServletRequest req) {
		 QueryWrapper<CpkConfig> queryWrapper = QueryGenerator.initQueryWrapper(cpkConfig, req.getParameterMap());
		 return Result.OK(cpkConfigService.list(queryWrapper));
	 }
}
