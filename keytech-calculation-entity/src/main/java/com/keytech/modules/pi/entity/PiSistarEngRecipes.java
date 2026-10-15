package com.keytech.modules.pi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: Adapter配方表
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_sistar_eng_recipes")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Adapter配方表")
public class PiSistarEngRecipes implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
	@Excel(name = "工厂Code", width = 15)
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门Code", width = 15)
    @Schema(description = "部门Code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线Code", width = 15)
    @Schema(description = "产线Code")
    private String lineCode;

	/**siteNo*/
	@Excel(name = "siteNo", width = 15)
    @Schema(description = "siteNo")
    private Integer siteNo;

	/**areaNo*/
	@Excel(name = "areaNo", width = 15)
    @Schema(description = "areaNo")
    private Integer areaNo;

	/**reccatNo*/
	@Excel(name = "reccatNo", width = 15)
    @Schema(description = "reccatNo")
    private Integer reccatNo;

	/**recipeNo*/
	@Excel(name = "recipeNo", width = 15)
    @Schema(description = "recipeNo")
    private Integer recipeNo;

	/**recipeName*/
	@Excel(name = "recipeName", width = 15)
    @Schema(description = "recipeName")
    private String recipeName;

	/**recipeLongName*/
	@Excel(name = "recipeLongName", width = 15)
    @Schema(description = "recipeLongName")
    private String recipeLongName;

	/**nominalSize*/
	@Excel(name = "nominalSize", width = 15)
    @Schema(description = "nominalSize")
    private Double nominalSize;

	/**recprocNo*/
	@Excel(name = "recprocNo", width = 15)
    @Schema(description = "recprocNo")
    private Integer recprocNo;

	/**recprocName*/
	@Excel(name = "recprocName", width = 15)
    @Schema(description = "recprocName")
    private String recprocName;

	/**productId*/
	@Excel(name = "productId", width = 15)
    @Schema(description = "productId")
    private Integer productId;

	/**createUser*/
	@Excel(name = "createUser", width = 15)
    @Schema(description = "createUser")
    private String createUser;

	/**updateUser*/
	@Excel(name = "updateUser", width = 15)
    @Schema(description = "updateUser")
    private String updateUser;

	/**更新时间戳*/
	@Excel(name = "更新时间戳", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间戳")
    private Date updateTimestamp;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**0不删除,1为删除*/
	@Excel(name = "0不删除,1为删除", width = 15)
    @Schema(description = "0不删除,1为删除")
    @TableLogic
    private Integer delFlag;

	/**数据创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据创建时间")
    private Date createTime;

	/**数据更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据更新时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    private static final long serialVersionUID = 1L;
}
