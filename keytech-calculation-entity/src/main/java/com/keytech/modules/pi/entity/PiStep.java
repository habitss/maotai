package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 步骤管理
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */

@Data
@Accessors(chain = true)
@TableName("mes3_pi_step")
@Schema(description="步骤管理")
public class PiStep extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;


	/**设备id*/
	@Excel(name = "设备id", width = 15)
    @Schema(description="设备id")
    @Dict(dictTable ="mes3_mdm_device",dicText = "device_name",dicCode = "id")
    private java.lang.Integer deviceId;
	/**步骤号*/
	@Excel(name = "步骤号", width = 15)
    @Schema(description="步骤号")
    private java.lang.String stepNum;
	/**步骤名称*/
	@Excel(name = "步骤名称", width = 15)
    @Schema(description="步骤名称")
    private java.lang.String stepName;


    /**查询部门*/
    @TableField(exist = false)
    private String queryDept;
}
