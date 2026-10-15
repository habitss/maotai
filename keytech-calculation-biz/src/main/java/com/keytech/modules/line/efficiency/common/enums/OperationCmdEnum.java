package com.keytech.modules.line.efficiency.common.enums;

/**
 * @FileName OperationCmdEnum
 * @Description
 * @Author HQ
 * @date 2025-11-13
 **/
public enum OperationCmdEnum {
    OCMD_CLOSE("OCMD_CLOSE", "关"),
    OCMD_OPEN("OCMD_OPEN", "开"),
    OCMD_JUMP_SEQ_REQ("OCMD_JUMPSEQREQ", "跳步"),
    OCMD_RESTART("OCMD_RESTART", "程序重启"),
    OCMD_STOP("OCMD_STOP", "电机停止"),
    OCMD_UNLOCK_REL("MODE.OCMD_UNLOCKREL", "电机解锁"),
    OCMD_ACQ_LOCK("MODE.OCMD_ACQLOCK", "电机锁定"),
    OSET_JUMP_SEQ_NR("OSET_JUMPSEQNR", "程序跳步"),
    OSET_SPEED_REF("OSET_SPEEDREF", "变频器设定速度"),
    OSET_CV("OSET_CV", "比例阀设定开度"),
    OCMD_ST0("OCMD_ST0", "双座阀关"),
    OCMD_ST1("OCMD_ST1", "双座阀开"),
    SET_REQ_RCP_INDEX("SET_REQRCPINDEX", "选择配方"),
    SET_RCP_TYPE("SET_RCPTYPE", "选择配方类型"),
    CFG_RCP_TYPE("CFG_RCPTYPE", "选择配方类型"),
    STATUS_TARGET_USER_CID("STATUS.VAL_TARGETUSERCID", "画面赋值"),
    MODE_MCMD_ACQ("MODE.MCMD_ACQ", "维护模式"),
    // Bop[*****].Parameter[****]   *范围在1-31
    BOP_PARAMETER("BOP.PARAMETER", "修改配方参数"),
    OCMD_START("OCMD_START", "手动模式-电机启动"),
    CFG_PVEUMAX("CFG_PVEUMAX", "选择"),
    MODE_MCMD_REL("MODE.MCMD_REL", "释放维护模式"),
    PVEU("PVEU", "配置模拟量上下限"),
    OCMD_ABORT("OCMD_ABORT", "手动模式-结束程序"),
    //Cfg_Par[*]    修改步骤参数
    CFG_PAR("CFG_PAR", "手动模式-结束程序"),
    OCMD_HOLD("OCMD_HOLD ", "手动模式-暂停程序");


    private final String code;
    private final String name;

    OperationCmdEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据code获取名称 - 主要方法
     * 支持大小写不敏感的匹配
     */
    public static String getNameByCode(String code) {
        if (code == null) {
            return "";
        }
        String upperCode = code.toUpperCase();
        for (OperationCmdEnum cmd : OperationCmdEnum.values()) {
            if (cmd.getCode().equals(upperCode)) {
                return cmd.getName();
            }
        }
        return "";
    }

    /**
     * 根据code获取枚举
     */
    public static OperationCmdEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        String upperCode = code.toUpperCase();
        for (OperationCmdEnum cmd : OperationCmdEnum.values()) {
            if (cmd.getCode().equals(upperCode)) {
                return cmd;
            }
        }
        return null;
    }

    /**
     * 检查code是否存在
     */
    public static boolean contains(String code) {
        return getByCode(code) != null;
    }
}
