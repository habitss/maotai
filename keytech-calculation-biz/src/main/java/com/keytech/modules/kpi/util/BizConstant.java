package com.keytech.modules.kpi.util;

import org.jeecg.common.constant.CommonConstant;

import java.util.regex.Pattern;

/**
 * @author jacksparrow
 * @version v1.0.0
 * @date 2023/9/19 10:43
 */
public interface BizConstant extends CommonConstant {

    Pattern FormulaRegex = Pattern.compile("\\⊙([^⊙]+)\\⊙");

    String PI_PASS_RATE = "PassRate:";

    String PI_BASE_DATA_TYPE_DICT = "mdm_pi_base_data_type:code:type_name";
    String PI_DEVICE_DATA_TYPE_DICT = "mdm_pi_device:id:device_name";

    String PI_BASE_HEAD_CONFIG_DICT = "mdm_pi_base_head_config:key:title";

    String PI_BASE_TYPE_CONFIG_DICT = "mdm_pi_base_head_config:mdm_pi_base_data_type:key:type_name";

    String SYS_USER_MAP = "userMap";
    String BREED_MAP = "breedMap";
    String KPI_TYPE_DATA_DICT = "mes3_kpi_tye:id:kpi_type";

    String MesPostDataMapping = "mesPostDataMappingMappings";

    String COP_DEVICE_DATA_TYPE_DICT = "mdm_cop_device:id:device_name";

    String COP_CELL_DATA_TYPE_DICT = "mdm_cell:id:cell_name";
    String PI_Example = "example";

    String PI_MAPPING_DICT = "mdm_pi_mapping_v2:pi_code:pi_name";

    String APPARATUS_NAME_DATA_TYPE_DICT = "mdm_period_verification_record:id:apparatus_name";

    String MES_TENANT_ID="x-tenant-id";


     String ok = "completed"; //已完成
     String end = "postpone"; //延迟
     String ing = "underWay"; //进行中
     String cancel = "cancel"; //取消
     String delayOk = "delayOk"; //延迟完成

    String POST_CHECK_LIST_DATA_TYPE_DICT = "mdm_post_check_list_config:id:check_table_name";

    String[] MONTHS = new String[]{"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
    Integer[] MONTHSNUM = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

    String X_ACCESS_CLIENT_TYPE = "X-ACCESS-CLIENT-TYPE";
    String ACCESS_CLIENT_TYPE = "X-APP";
}
