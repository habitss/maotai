package com.keytech.modules.pi.dto;

import lombok.Data;

/**
 * @Description: 配方/设备/步骤 抽取数据DTO
 */
@Data
public class RecipeExtractDto {

    /**区域号*/
    private Integer areaNo;

    /**配方号*/
    private Integer recipeNo;

    /**配方名*/
    private String recipeName;

    /**设备号*/
    private Integer unitNo;

    /**设备名*/
    private String unitName;

    /**步骤号*/
    private Integer eopNo;

    /**步序*/
    private Integer stepInProcedure;

    /**步骤名*/
    private String eopName;
}
