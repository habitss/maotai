package com.keytech.modules.meeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: mes3_meeting_links
 * @Author: jeecg-boot
 * @Date:   2025-05-29
 * @Version: V1.0
 */
@Data
@TableName("mes3_meeting_links")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="mes3_meeting_links")
public class MeetingLinks implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**link*/
	@Excel(name = "link", width = 15)
    @Schema(description = "link")
    private String link;

	/**linkAddress*/
	@Excel(name = "linkAddress", width = 15)
    @Schema(description = "linkAddress")
    private String linkAddress;

	/**templateId*/
	@Excel(name = "templateId", width = 15)
    @Schema(description = "templateId")
    private String templateId;

    private static final long serialVersionUID = 1L;
}
