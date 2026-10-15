package com.keytech.modules.manage.check.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import com.keytech.modules.kpi.util.TreeListUtil;
import com.keytech.modules.manage.check.entity.ManageCheckCategory;
import com.keytech.modules.manage.check.service.IManageCheckCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 管理者检查清单-分层检查分类
 * @Author: jeecg-boot
 * @Date:   2025-03-30
 * @Version: V1.0
 */
@Tag(name="管理者检查清单-分层检查分类")
@RestController
@RequestMapping("/manage/check/manageCheckCategory")
@Slf4j
public class ManageCheckCategoryController extends BaseController<IManageCheckCategoryService, ManageCheckCategory> {
	@Autowired
	private IManageCheckCategoryService manageCheckCategoryService;

	/**
	 * 分页列表查询
	 *
	 * @param manageCheckCategory
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="管理者检查清单-分层检查分类-分页列表查询")
	@GetMapping(value = "/rootList")
	public Result<IPage<ManageCheckCategory>> queryPageList(ManageCheckCategory manageCheckCategory,
															@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
															@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
															@RequestParam(name = "orderField",required = false) String orderField,
															@RequestParam(name = "orderType",required = false) String orderType,
															@RequestParam(name = "selectFields",required = false) String selectFields,
															HttpServletRequest req) {
		String hasQuery = req.getParameter("hasQuery");
        if(hasQuery != null && "true".equals(hasQuery)){
            QueryWrapper<ManageCheckCategory> queryWrapper =  QueryGenerator.initQueryWrapper(manageCheckCategory, req.getParameterMap());
            List<ManageCheckCategory> list = manageCheckCategoryService.queryTreeListNoPage(queryWrapper);
            IPage<ManageCheckCategory> pageList = new Page<>(1, 200, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        }else{
            String parentId = manageCheckCategory.getPid();
            if (oConvertUtils.isEmpty(parentId)) {
                parentId = "0";
            }
            manageCheckCategory.setPid(null);
            QueryWrapper<ManageCheckCategory> queryWrapper = QueryGenerator.initQueryWrapper(manageCheckCategory, req.getParameterMap());
            // 使用 eq 防止模糊查询
            queryWrapper.eq("pid", parentId);
            Page<ManageCheckCategory> page = new Page<ManageCheckCategory>(pageNo, pageSize);
            IPage<ManageCheckCategory> pageList = manageCheckCategoryService.page(page, queryWrapper);
            return Result.OK(pageList);
        }
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
			 List<SelectTreeModel> ls = manageCheckCategoryService.queryListByPid(pid);
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
			 List<SelectTreeModel> ls = manageCheckCategoryService.queryListByPid(pcode);
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
			 List<SelectTreeModel> temp = manageCheckCategoryService.queryListByPid(tsm.getKey());
			 if (temp != null && temp.size() > 0) {
				 tsm.setChildren(temp);
				 loadAllChildren(temp);
			 }
		 }
	 }

	 /**
      * 获取子数据
      * @param manageCheckCategory
      * @param req
      * @return
      */
	@Operation(summary="管理者检查清单-分层检查分类-获取子数据")
	@GetMapping(value = "/childList")
	public Result<IPage<ManageCheckCategory>> queryPageList(ManageCheckCategory manageCheckCategory,HttpServletRequest req) {
		QueryWrapper<ManageCheckCategory> queryWrapper = QueryGenerator.initQueryWrapper(manageCheckCategory, req.getParameterMap());
		List<ManageCheckCategory> list = manageCheckCategoryService.list(queryWrapper);
		IPage<ManageCheckCategory> pageList = new Page<>(1, 10, list.size());
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
    @Operation(summary="管理者检查清单-分层检查分类-批量获取子数据")
    @GetMapping("/getChildListBatch")
    public Result getChildListBatch(@RequestParam("parentIds") String parentIds) {
        try {
            QueryWrapper<ManageCheckCategory> queryWrapper = new QueryWrapper<>();
            List<String> parentIdList = Arrays.asList(parentIds.split(","));
            queryWrapper.in("pid", parentIdList);
            List<ManageCheckCategory> list = manageCheckCategoryService.list(queryWrapper);
            IPage<ManageCheckCategory> pageList = new Page<>(1, 10, list.size());
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
	 * @param manageCheckCategory
	 * @return
	 */
	@AutoLog(value = "管理者检查清单-分层检查分类-添加")
	@Operation(summary="管理者检查清单-分层检查分类-添加")
    @RequiresPermissions("manage.check:mes3_manage_check_category:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ManageCheckCategory manageCheckCategory) {
		manageCheckCategoryService.addManageCheckCategory(manageCheckCategory);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param manageCheckCategory
	 * @return
	 */
	@AutoLog(value = "管理者检查清单-分层检查分类-编辑")
	@Operation(summary="管理者检查清单-分层检查分类-编辑")
    @RequiresPermissions("manage.check:mes3_manage_check_category:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ManageCheckCategory manageCheckCategory) {
		manageCheckCategoryService.updateManageCheckCategory(manageCheckCategory);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "管理者检查清单-分层检查分类-通过id删除")
	@Operation(summary="管理者检查清单-分层检查分类-通过id删除")
    @RequiresPermissions("manage.check:mes3_manage_check_category:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		manageCheckCategoryService.deleteManageCheckCategory(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "管理者检查清单-分层检查分类-批量删除")
	@Operation(summary="管理者检查清单-分层检查分类-批量删除")
    @RequiresPermissions("manage.check:mes3_manage_check_category:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.manageCheckCategoryService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="管理者检查清单-分层检查分类-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ManageCheckCategory> queryById(@RequestParam(name="id",required=true) String id) {
		ManageCheckCategory manageCheckCategory = manageCheckCategoryService.getById(id);
		if(manageCheckCategory==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(manageCheckCategory);
	}

	 /**
	  * 获取所有数据
	  *
	  * @param manageCheckCategory
	  * @return
	  */
	 @Operation(summary="管理者检查清单-分层检查分类-所有数据查询")
	 @GetMapping(value = "/allData")
	 public Result<List<ManageCheckCategory>> queryAllList(ManageCheckCategory manageCheckCategory) {
		 return Result.OK(manageCheckCategoryService.list());
	 }

	/**
	 * 分层检查分类-获取所有树数据
	 * @param disabled 是否需要禁用
	 * @return
	 */
	@Operation(summary="分层检查分类-获取所有树数据")
	@GetMapping(value = "/queryAllTreeData")
	public Result<List<ManageCheckCategory>> queryAllTreeData(boolean disabled){
		List<ManageCheckCategory> list = manageCheckCategoryService.list();
		for(ManageCheckCategory a : list){
			if("0".equals(a.getPid())){
				a.setPid(null);
			}
			if(disabled){
				a.setDisabled(a.getIfEnable() == 1 ? false : true);
			}
		}
		return Result.OK(TreeListUtil.getTreeList(list, ManageCheckCategory::getId, ManageCheckCategory::getPid, ManageCheckCategory::getChildList).stream()
				.filter(M -> Objects.isNull(M.getPid()))
				.collect(Collectors.toList()));
	}
}
