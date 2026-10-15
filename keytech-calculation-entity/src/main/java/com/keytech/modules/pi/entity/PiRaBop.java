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
 * @Description: RA-步骤字典
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_ra_bop")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="RA-步骤字典")
public class PiRaBop implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Integer id;

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

	/**uid*/
	@Excel(name = "uid", width = 15)
    @Schema(description = "uid")
    private Integer uid;

	/**unitUid*/
	@Excel(name = "unitUid", width = 15)
    @Schema(description = "unitUid")
    private Integer unitUid;

	/**bopId*/
	@Excel(name = "bopId", width = 15)
    @Schema(description = "bopId")
    private Integer bopId;

	/**nameCn*/
	@Excel(name = "nameCn", width = 15)
    @Schema(description = "nameCn")
    private String nameCn;

	/**nameEn*/
	@Excel(name = "nameEn", width = 15)
    @Schema(description = "nameEn")
    private String nameEn;

	/**descCn*/
	@Excel(name = "descCn", width = 15)
    @Schema(description = "descCn")
    private String descCn;

	/**descEn*/
	@Excel(name = "descEn", width = 15)
    @Schema(description = "descEn")
    private String descEn;

	/**version1*/
	@Excel(name = "version1", width = 15)
    @Schema(description = "version1")
    private Integer version1;

	/**version2*/
	@Excel(name = "version2", width = 15)
    @Schema(description = "version2")
    private Integer version2;

	/**versionInfo*/
	@Excel(name = "versionInfo", width = 15)
    @Schema(description = "versionInfo")
    private String versionInfo;

	/**bk1*/
	@Excel(name = "bk1", width = 15)
    @Schema(description = "bk1")
    private String bk1;

	/**bk2*/
	@Excel(name = "bk2", width = 15)
    @Schema(description = "bk2")
    private String bk2;

	/**accessPath*/
	@Excel(name = "accessPath", width = 15)
    @Schema(description = "accessPath")
    private String accessPath;

	/**imageKey*/
	@Excel(name = "imageKey", width = 15)
    @Schema(description = "imageKey")
    private String imageKey;

    /**区域ID*/
    @Excel(name = "区域ID", width = 15)
    @Schema(description = "区域ID")
    private Integer areaId;

    /**设备ID*/
    @Excel(name = "设备ID", width = 15)
    @Schema(description = "设备ID")
    private Integer unitId;

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
