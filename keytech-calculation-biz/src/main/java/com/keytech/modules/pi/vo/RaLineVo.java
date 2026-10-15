package com.keytech.modules.pi.vo;

import lombok.Data;

/**
 * @FileName RaLineVO
 * @Description Ra-产线
 **/
@Data
public class RaLineVo {

    /**
     * 产线id
     */
    private Integer id;

    /**
     * 产线名称
     */
    private String nameCn;

    /**
     * 区域id
     */
    private Integer areaId;

}
