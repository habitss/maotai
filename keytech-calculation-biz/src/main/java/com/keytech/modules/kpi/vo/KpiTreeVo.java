package com.keytech.modules.kpi.vo;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sh
 * @date 2023/03/21/15:12
 */

@Data
public class KpiTreeVo {

    private String id;
    private String parentId;
    private String name;
    private String value;
    private int level;
    private List<KpiTreeVo> children = new ArrayList<>();
}
