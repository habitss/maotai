package com.keytech.modules.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: mes3_user_only_dept
 * @Author: jeecg-boot
 * @Date:   2025-05-16
 * @Version: V1.0
 */
@Data
@TableName("mes3_user_only_dept")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="用户唯一部门")
public class UserOnlyDept implements Serializable {


	/**主键id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键id")
    private String id;

    @Schema(description = "工厂Code")
    private String factoryCode;

    /**用户id*/
    @Excel(name = "用户id", width = 15)
    @Schema(description = "用户id")
    private String userId;

    /**姓名*/
    @Excel(name = "姓名", width = 15)
    @Schema(description = "姓名")
    private String realname;

	/**部门id*/
	@Excel(name = "部门id", width = 15)
    @Schema(description = "部门id")
    private String userDeptId;

	/**部门编码*/
	@Excel(name = "部门编码", width = 15)
    @Schema(description = "部门编码")
    private String userDeptCode;

	/**部门名称*/
	@Excel(name = "部门名称", width = 15)
    @Schema(description = "部门名称")
    private String userDeptName;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
