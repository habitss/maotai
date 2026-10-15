package com.keytech.modules.post.check.controller;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.post.check.dto.PostCheckRecordConfigQueryDto;
import com.keytech.modules.post.check.entity.PostCheckRecordConfigProject;
import com.keytech.modules.post.check.entity.PostCheckRecordConfigStandard;
import com.keytech.modules.post.check.service.IPostCheckRecordConfigMainService;
import com.keytech.modules.post.check.service.IPostCheckRecordConfigProjectService;
import com.keytech.modules.post.check.service.IPostCheckRecordConfigStandardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 岗位检查记录配置-检查项目
 * @Author: jeecg-boot
 * @Date:   2025-01-06
 * @Version: V1.0
 */
@Tag(name="岗位检查记录配置-检查项目")
@RestController
@RequestMapping("/post_check/postCheckRecordConfigProject")
@Slf4j
public class PostCheckRecordConfigProjectController extends BaseController<IPostCheckRecordConfigProjectService, PostCheckRecordConfigProject> {
	@Autowired
	private IPostCheckRecordConfigProjectService postCheckRecordConfigProjectService;

	@Autowired
	private IPostCheckRecordConfigStandardService postCheckRecordConfigStandardService;

	@Autowired
	private IMdmDictService mdmDictService;
	
	/**
	 * 分页列表查询
	 *
	 * @param postCheckRecordPzJcxm
	 * @param req
	 * @return
	 */
	@Operation(summary="岗位检查记录配置-检查项目-分页列表查询")
	@GetMapping(value = "/queryList")
	public Result<List<PostCheckRecordConfigQueryDto>> queryPageList(PostCheckRecordConfigProject postCheckRecordPzJcxm,
																	 HttpServletRequest req) throws InvocationTargetException, IllegalAccessException {
		List<PostCheckRecordConfigQueryDto> jcxmDtoList = new ArrayList<>(0);
		//获取检查项目数据
        QueryWrapper<PostCheckRecordConfigProject> queryWrapper = QueryGenerator.initQueryWrapper(postCheckRecordPzJcxm, req.getParameterMap());
		List<PostCheckRecordConfigProject> jcxmList = postCheckRecordConfigProjectService.list(queryWrapper);

		if(CollectionUtil.isNotEmpty(jcxmList)){
			//获取检查项目Id
			String[] idArray = jcxmList.stream()
					.map(PostCheckRecordConfigProject::getId)
					.collect(Collectors.toList())
					.toArray(new String[0]);
			//根据检查项目Id获取检查标准数据
			LambdaQueryWrapper<PostCheckRecordConfigStandard> lambdaQueryWrapper = new LambdaQueryWrapper<>();
			lambdaQueryWrapper.in(PostCheckRecordConfigStandard::getJcxmId, Arrays.asList(idArray));
			lambdaQueryWrapper.orderByAsc(PostCheckRecordConfigStandard::getJcxmId);
			lambdaQueryWrapper.orderByAsc(PostCheckRecordConfigStandard::getSort);
			lambdaQueryWrapper.orderByAsc(PostCheckRecordConfigStandard::getId);
			List<PostCheckRecordConfigStandard> jcbzList = postCheckRecordConfigStandardService.list(lambdaQueryWrapper);

			//组装数据，将检查标准数据放入到对应的检查项目中
			for(PostCheckRecordConfigProject jcxm : jcxmList){
				List<PostCheckRecordConfigQueryDto> tempDtoList = new ArrayList<>(0);
				for(PostCheckRecordConfigStandard jcbz : jcbzList){
					if(jcxm.getId().equals(jcbz.getJcxmId())){
						PostCheckRecordConfigQueryDto dto = new PostCheckRecordConfigQueryDto();
						BeanUtils.copyProperties(dto, jcbz);
						BeanUtils.copyProperties(dto, jcxm);
						dto.setRowSpan(0);
						dto.setBzid(jcbz.getId());
						dto.setSort(jcbz.getSort());
						tempDtoList.add(dto);
					}
				}
				if(CollectionUtil.isNotEmpty(tempDtoList)){
					//处理需要合并的行数
					tempDtoList.get(0).setRowSpan(tempDtoList.size());
					jcxmDtoList.addAll(tempDtoList);
				}
			}
		}
		return Result.OK(jcxmDtoList);
	}
	
	/**
	 *   添加
	 *
	 * @param list
	 * @return
	 */
	@AutoLog(value = "岗位检查记录配置-检查项目-添加")
	@Operation(summary="岗位检查记录配置-检查项目-添加")
	@RequiresPermissions("post_check:mes3_post_check_record_config:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody List<PostCheckRecordConfigProject> list) {
		postCheckRecordConfigProjectService.savePostCheckPz(list);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  编辑
	 *
	 * @param postCheckRecordPzJcxm
	 * @return
	 */
	@AutoLog(value = "岗位检查记录配置-检查项目-编辑")
	@Operation(summary="岗位检查记录配置-检查项目-编辑")
	@RequiresPermissions("post_check:mes3_post_check_record_config:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PostCheckRecordConfigProject postCheckRecordPzJcxm) {
		postCheckRecordConfigProjectService.updateById(postCheckRecordPzJcxm);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id 项目id
	 * @param bzid 标准id
	 * @return
	 */
	@AutoLog(value = "岗位检查记录配置-检查项目-通过id删除")
	@Operation(summary="岗位检查记录配置-检查项目-通过id删除")
	@RequiresPermissions("post_check:mes3_post_check_record_config:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id", required = false) String id, @RequestParam(name="bzid") String bzid) {
		if(StrUtil.isNotBlank(id)){
			postCheckRecordConfigProjectService.removeById(id);
		}
		postCheckRecordConfigStandardService.removeById(bzid);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "岗位检查记录配置-检查项目-批量删除")
	@Operation(summary="岗位检查记录配置-检查项目-批量删除")
	@RequiresPermissions("post_check:mes3_post_check_record_config:delete")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids, @RequestParam(name="bzIds",required=true) String bzIds) {
		this.postCheckRecordConfigProjectService.deleteBatchData(ids, bzIds);
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="岗位检查记录配置-检查项目-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PostCheckRecordConfigProject> queryById(@RequestParam(name="id",required=true) String id) {
		PostCheckRecordConfigProject postCheckRecordPzJcxm = postCheckRecordConfigProjectService.getById(id);
		if(postCheckRecordPzJcxm==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(postCheckRecordPzJcxm);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param postCheckRecordPzJcxm
    */
    @RequiresPermissions("post_check:mes3_post_check_record_config:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PostCheckRecordConfigProject postCheckRecordPzJcxm, String type) {
		String exportFields = "";
		//根据类型配置导出字段
		if(type.equals("2")){
			exportFields = "id,factoryCode,deptCode,lineCode,zbId,step,checkItem,checkStandardDesc,listJcbz";
		}else{
			exportFields = "id,factoryCode,deptCode,lineCode,zbId,step,checkItem,checkFrequency,frequencyIllustrate,frequencyIllustrateCount,listJcbz";
		}

		//获取封装好的导出对象
		ModelAndView mv = super.exportXls(request, postCheckRecordPzJcxm, PostCheckRecordConfigProject.class, "岗位检查记录配置-检查项目", exportFields);
		//获取导出数据
		List<PostCheckRecordConfigProject> jcxmList = (List<PostCheckRecordConfigProject>) mv.getModel().get("data");
		if(CollectionUtil.isNotEmpty(jcxmList)){
			//获取检查项目Id
			String[] idArray = jcxmList.stream()
					.map(PostCheckRecordConfigProject::getId)
					.collect(Collectors.toList())
					.toArray(new String[0]);
			//根据检查项目Id获取检查标准数据
			LambdaQueryWrapper<PostCheckRecordConfigStandard> lambdaQueryWrapper = new LambdaQueryWrapper<>();
			lambdaQueryWrapper.in(PostCheckRecordConfigStandard::getJcxmId, Arrays.asList(idArray));
			lambdaQueryWrapper.orderByAsc(PostCheckRecordConfigStandard::getSort);
			List<PostCheckRecordConfigStandard> jcbzList = postCheckRecordConfigStandardService.list(lambdaQueryWrapper);

			//组装数据，将检查标准数据放入到对应的检查项目中
			for(PostCheckRecordConfigProject jcxm : jcxmList){
				List<PostCheckRecordConfigStandard> listJcbz = new ArrayList<>(0);
				for(PostCheckRecordConfigStandard jcbz : jcbzList){
					if(jcxm.getId().equals(jcbz.getJcxmId())){
						listJcbz.add(jcbz);
					}
				}
				jcxm.setListJcbz(listJcbz);
			}
			//重新放入数据到导出对象中
			mv.addObject("data", jcxmList);
		}
		return mv;
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("post_check:mes3_post_check_record_config:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return importExcelBy(request, response, PostCheckRecordConfigProject.class);
    }

	/**
	 * 模板导入
	 * @param request
	 * @param response
	 * @param clazz
	 * @return
	 * @throws IOException
	 */
	private Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<PostCheckRecordConfigProject> clazz) throws IOException {
		//获取导入文件
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

		String zbId = request.getParameter("zbId");
		String reportType = request.getParameter("reportType");
		//错误信息集合
		List<String> errorMessage = new ArrayList<>();
		//成功数，错误数
		int successLines = 0, errorLines = 0;
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			// 获取上传文件对象
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				//获取检查项目数据
				List<PostCheckRecordConfigProject> projectList = postCheckRecordConfigProjectService
						.lambdaQuery().eq(PostCheckRecordConfigProject::getZbId, zbId)
						.list();
				Map<String, PostCheckRecordConfigProject> projectMap = CollStreamUtil.toMap(projectList, PostCheckRecordConfigProject::getId, bean -> bean);

				//获取检查标准
				Map<String, PostCheckRecordConfigStandard> standardMap = new HashMap<>();
				if(CollectionUtil.isNotEmpty(projectList)){
					List<PostCheckRecordConfigStandard> standardList =
							postCheckRecordConfigStandardService
									.lambdaQuery()
									.in(PostCheckRecordConfigStandard::getJcxmId,
											projectList.stream()
													.map(PostCheckRecordConfigProject::getId)
													.collect(Collectors.toList())
									)
									.list();
					standardMap = CollStreamUtil.toMap(standardList, PostCheckRecordConfigStandard::getId, bean -> bean);
				}
				//获取检查数据记录格式字典
				List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(List.of("MES3_POST_CHECK_FIELD_TYPE"));
				Map<String, MdmDict> dictMap = CollStreamUtil.toMap(mdmDictList, MdmDict::getItemVal, bean -> bean);

				//获取上传文件中的数据
				List<PostCheckRecordConfigProject> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);

				List<PostCheckRecordConfigStandard> standList = new ArrayList<>();
				DefaultIdentifierGenerator idGen = new DefaultIdentifierGenerator();
				int lineNumber = 3;
				for(int i = 0; i < list.size(); i++){
					PostCheckRecordConfigProject data = list.get(i);
					data.setZbId(zbId);
					lineNumber++;

					//如果项目数据的id不为空，且又不属于当前配置，置空id
					if(StrUtil.isNotBlank(data.getId()) && !projectMap.containsKey(data.getId())){
						data.setId(null);
					}

					//如果项目数据的id为空，代表新增数据赋予新id
					if(null == data.getId()){
						data.setId(String.valueOf(idGen.nextId(null)));
					}

					if(null == data.getCheckItem()){
						errorLines++;
						errorMessage.add("第 " + lineNumber + " 行：检查项目为空，忽略导入。");
						continue;
					}

					//报表类型：1、通用岗位记录；2、岗位检查清单
					if("1".equals(reportType)){
						if(null == data.getCheckFrequency()){
							errorLines++;
							errorMessage.add("第 " + lineNumber + " 行：项目检查频次为空，忽略导入。");
							continue;
						}
						if(null == data.getFrequencyIllustrate()){
							errorLines++;
							errorMessage.add("第 " + lineNumber + " 行：标准检查频次说明为空，忽略导入。");
							continue;
						}
						if(null == data.getFrequencyIllustrateCount()){
							errorLines++;
							errorMessage.add("第 " + lineNumber + " 行：标准检查频次为空，忽略导入。");
							continue;
						}
					}
					for(int j = 0; j < data.getListJcbz().size(); j++){
						lineNumber += j;
						PostCheckRecordConfigStandard jcbzData = data.getListJcbz().get(j);

						//如果检查标准数据的id不为空，且又不属于当前配置，置空id
						if(StrUtil.isNotBlank(jcbzData.getId()) && !standardMap.containsKey(jcbzData.getId())){
							jcbzData.setId(null);
						}

						//如果检查标准数据的id为空，代表新增数据赋予新id
						if(null == jcbzData.getJcxmId()){
							jcbzData.setJcxmId(data.getId());
						}

						if(null == jcbzData.getCheckname()){
							errorLines++;
							errorMessage.add("第 " + lineNumber + " 行：检查标准名称为空，忽略导入。");
							continue;
						}
						if(StrUtil.isEmpty(jcbzData.getChecknameFormat())){
							jcbzData.setChecknameFormat("3");
						}else{
							MdmDict dict = dictMap.get(jcbzData.getChecknameFormat());
							if(null == dict){
								errorLines++;
								errorMessage.add("第 " + lineNumber + " 行：检查数据记录格式不存在，忽略导入。");
								continue;
							}
						}
						standList.add(jcbzData);
					}
					successLines++;
				}
				baseService.saveOrUpdateBatch(list);
				postCheckRecordConfigStandardService.saveOrUpdateBatch(standList);
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
}
