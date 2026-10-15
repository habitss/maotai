package com.keytech.modules.pi.vo;

import lombok.Data;

import java.util.Date;
import java.util.Objects;

/**
 * 订单-批次
 */
@Data
public class OrderBatchVo {

    private Integer orderNo;
    private Integer batchNo;
    private Integer areaNo;

    private Date startTime;
    private Date endTime;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderBatchVo that = (OrderBatchVo) o;
        return Objects.equals(orderNo, that.orderNo) &&
                Objects.equals(batchNo, that.batchNo) &&
                Objects.equals(areaNo, that.areaNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNo, batchNo, areaNo);
    }
}
