package com.keytech.modules.pi.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.pi.entity.PiBasePoint;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: PI曲线-设备
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
@Data
public class PiCurveUnits{

	/**unitNo*/
    private String unitNo;

	/**unitName*/
    private String unitName;

    /**points 集合*/
    private List<PiBasePoint> points;

}
