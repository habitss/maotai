package com.keytech.modules.pi.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.entity.PiPost;
import com.keytech.modules.pi.service.IPiCustomizeTableFieldService;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import com.keytech.modules.pi.service.IPiPostService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * &#064;Description:  PI岗位绑定
 * &#064;Author:  jeecg-boot
 * &#064;Date:    2025-07-14
 * &#064;Version:  V1.0
  */
@Tag(name="PI岗位绑定")
@RestController
@RequestMapping("/pi/piPost")
@Slf4j
public class PiPostController extends BaseController<IPiPostService, PiPost> {
	@Autowired
	private IPiPostService piPostService;
	@Autowired
	private IPiCustomizeTableService piCustomizeTableService;
	/**
	 * 分页列表查询
	 *
	 * @param piPost 查询参数
	 * @param pageNo 页码
	 * @param pageSize 每页大小
	 * @param req 请求参数
	 * @return Result<IPage<E>>
	 */
	@Operation(summary="PI岗位绑定-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiPost>> queryPageList(PiPost piPost,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiPost> queryWrapper = QueryGenerator.initQueryWrapper(piPost, req.getParameterMap());
		Page<PiPost> page = new Page<>(pageNo, pageSize);
		IPage<PiPost> pageList = piPostService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * app使用分页列表查询
	 *
	 * @param piPost 查询参数
	 * @param pageNo 页码
	 * @param pageSize 每页大小
	 * @param req 请求参数
	 * @return Result<IPage<E>>
	 */
	@Operation(summary="PI岗位绑定-app使用分页列表查询")
	@GetMapping(value = "/queryPageAppList")
	public Result<?> queryPageAppList(PiPost piPost,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   @RequestParam(name = "tableName") String tableName,
								   HttpServletRequest req) {
		return Result.OK(piPostService.queryPageAppList(piPost, pageNo, pageSize, tableName, req));
	}

	 /**
	  * 获取所有数据
	  *
	  * @param piPost 查询参数
	  * @param req 请求参数
	  * @return Result<List<PiPost>>
	  */
	 @Operation(summary="PI岗位绑定-获取所有数据")
	 @GetMapping(value = "/listAll")
	 @AutoDict
	 public Result<List<PiPost>> listAll(PiPost piPost, HttpServletRequest req) {
		 //查询关联数据
		 QueryWrapper<PiPost> queryWrapper = QueryGenerator.initQueryWrapper(piPost, req.getParameterMap());
		 List<PiPost> list = piPostService.list(queryWrapper);
		 if(CollUtil.isNotEmpty(list)){
			 //根据关联表查询pi自定义表数据
			 List<PiCustomizeTable> piCustomizeTableList = piCustomizeTableService.lambdaQuery()
					 .in(PiCustomizeTable::getTableName, list.stream().map(PiPost::getRelatedTable).toList())
					 .list();
			 Map<String, String> piCustomizeMap = piCustomizeTableList.stream()
					 .collect(Collectors.toMap(
							 PiCustomizeTable::getTableName,
							 PiCustomizeTable::getAutomatic
					 ));
			 list.forEach(p -> {
				 if(piCustomizeMap.containsKey(p.getRelatedTable())){
					 p.setAutomatic(piCustomizeMap.get(p.getRelatedTable()));
				 }
			 });
		 }
		 return Result.OK(list);
	 }
	
	/**
	 *   添加
	 *
	 * @param piPost 参数
	 * @return Result<String>
	 */
	@AutoLog(value = "PI岗位绑定-添加")
	@Operation(summary="PI岗位绑定-添加")
	@RequiresPermissions("pi:mes3_pi_post:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiPost piPost) {
		validExists(piPost);
		piPostService.save(piPost);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piPost 参数
	 * @return Result<String>
	 */
	@AutoLog(value = "PI岗位绑定-编辑")
	@Operation(summary="PI岗位绑定-编辑")
	@RequiresPermissions("pi:mes3_pi_post:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiPost piPost) {
		validExists(piPost);
		piPostService.updateById(piPost);
		return Result.OK("编辑成功!");
	}

	private void validExists(PiPost piPost) {
		boolean isExists = piPostService.lambdaQuery()
				.eq(PiPost::getDeptCode, piPost.getDeptCode())
				.eq(PiPost::getLineCode, piPost.getLineCode())
				.eq(PiPost::getRelatedTable, piPost.getRelatedTable())
				.eq(PiPost::getId, piPost.getId())
				.ne(StrUtil.isNotBlank(piPost.getId()), PiPost::getId, piPost.getId())
				.exists();
		if(isExists){
			throw new JeecgBootException("当前报表已绑定该岗位");
		}
	}

	/**
	 *   通过id删除
	 *
	 * @param id 主键
	 * @return Result<String>
	 */
	@AutoLog(value = "PI岗位绑定-通过id删除")
	@Operation(summary="PI岗位绑定-通过id删除")
	@RequiresPermissions("pi:mes3_pi_post:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		piPostService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids 主键
	 * @return Result<String>
	 */
	@AutoLog(value = "PI岗位绑定-批量删除")
	@Operation(summary="PI岗位绑定-批量删除")
	@RequiresPermissions("pi:mes3_pi_post:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
		this.piPostService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id 主键
	 * @return Result<PiPost>
	 */
	@Operation(summary="PI岗位绑定-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiPost> queryById(@RequestParam(name="id") String id) {
		PiPost piPost = piPostService.getById(id);
		if(piPost==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piPost);
	}

    /**
    * 导出excel
    *
    * @param request 请求参数
    * @param piPost 查询参数
    */
    @RequiresPermissions("pi:mes3_pi_post:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiPost piPost) {
        return super.exportXls(request, piPost, PiPost.class, "PI岗位绑定");
    }

    /**
      * 通过excel导入数据
    *
    * @param request 请求参数
    * @param response 响应参数
    * @return Result<String>
    */
    @RequiresPermissions("pi:mes3_pi_post:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PiPost.class);
    }

}
