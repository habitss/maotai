package com.keytech.modules.pi.dto;

import java.util.Objects;

/**
 * PI计算模型复合键类，按照基础点位，触发方式，触发值进行归类
 * @author aolei
 */
public class CompositeKey {
    public final String pointCode;
    public final String calculateType;
    public final String calculateValue;

    public CompositeKey(String pointCode, String calculateType, String calculateValue) {
        this.pointCode = pointCode;
        this.calculateType = calculateType;
        this.calculateValue = calculateValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        CompositeKey that = (CompositeKey) o;
        return Objects.equals(pointCode, that.pointCode) &&
                Objects.equals(calculateType, that.calculateType) &&
                Objects.equals(calculateValue, that.calculateValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointCode, calculateType, calculateValue);
    }
}
