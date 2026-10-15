package com.keytech.modules.practice.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.practice.dto.BestPracticeRecommendDto;
import com.keytech.modules.practice.entity.BestPracticeRecommend;
import com.keytech.modules.practice.entity.BestPracticeRecommendDetail;
import com.keytech.modules.practice.service.IBestPracticeRecommendDetailService;
import com.keytech.modules.practice.service.IBestPracticeRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 最佳实践推荐表
 * @Author: jeecg-boot
 * @Date:   2025-03-24
 * @Version: V1.0
 */
@Tag(name="最佳实践推荐表")
@RestController
@RequestMapping("/practice/bestPracticeRecommend")
@Slf4j
public class BestPracticeRecommendController extends BaseController<IBestPracticeRecommendService, BestPracticeRecommend> {
	@Autowired
	private IBestPracticeRecommendService bestPracticeRecommendService;

	@Autowired
	private IBestPracticeRecommendDetailService bestPracticeRecommendDetailService;
	
	/**
	 * 分页列表查询
	 *
	 * @param bestPracticeRecommend
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="最佳实践推荐表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<BestPracticeRecommend>> queryPageList(BestPracticeRecommend bestPracticeRecommend,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<BestPracticeRecommend> queryWrapper = QueryGenerator.initQueryWrapper(bestPracticeRecommend, req.getParameterMap());
		Page<BestPracticeRecommend> page = new Page<BestPracticeRecommend>(pageNo, pageSize);
		IPage<BestPracticeRecommend> pageList = bestPracticeRecommendService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "最佳实践推荐表-添加")
	@Operation(summary="最佳实践推荐表-添加")
	@RequiresPermissions("practice:mes3_best_practice_recommend:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody BestPracticeRecommendDto dto) {
		dto.setId(IdUtil.getSnowflakeNextIdStr());
		bestPracticeRecommendService.save(dto);
		if(CollUtil.isNotEmpty(dto.getDetailList())){
			for(BestPracticeRecommendDetail detail : dto.getDetailList()){
				detail.setRecommendId(dto.getId());
			}
			bestPracticeRecommendDetailService.saveBatch(dto.getDetailList());
		}
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "最佳实践推荐表-编辑")
	@Operation(summary="最佳实践推荐表-编辑")
	@RequiresPermissions("practice:mes3_best_practice_recommend:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BestPracticeRecommendDto dto) {
		bestPracticeRecommendService.updateById(dto);
		bestPracticeRecommendDetailService.saveOrUpdateBatch(dto.getDetailList());
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "最佳实践推荐表-通过id删除")
	@Operation(summary="最佳实践推荐表-通过id删除")
	@RequiresPermissions("practice:mes3_best_practice_recommend:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		bestPracticeRecommendService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "最佳实践推荐表-批量删除")
	@Operation(summary="最佳实践推荐表-批量删除")
	@RequiresPermissions("practice:mes3_best_practice_recommend:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bestPracticeRecommendService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	 /**
	  * 通过填报id查询
	  * @param fillId
	  * @return
	  */
	 @Operation(summary="最佳实践推荐表明细-通过填报id查询")
	 @GetMapping(value = "/queryByFillId")
	 public Result<BestPracticeRecommendDto> queryById(@RequestParam(name="fillId",required=true) String fillId) {
		 //根据填报id获取推荐表数据
		 List<BestPracticeRecommend> recommend = bestPracticeRecommendService.list(
				 new LambdaQueryWrapper<BestPracticeRecommend>().eq(BestPracticeRecommend::getFillId, fillId)
		 );
		 if(CollUtil.isEmpty(recommend)) {
			 return Result.OK();
		 }
		 //将数据拷贝至dto
		 BestPracticeRecommendDto dto = new BestPracticeRecommendDto();
		 BeanUtil.copyProperties(recommend.get(0), dto);

		 //根据推荐表id查询明细数据
		 List<BestPracticeRecommendDetail> detailList = bestPracticeRecommendDetailService.list(
				 new LambdaQueryWrapper<BestPracticeRecommendDetail>().eq(BestPracticeRecommendDetail::getRecommendId, dto.getId())
		 );
		 dto.setDetailList(detailList);
		 return Result.OK(dto);
	 }
}
