package com.keytech.modules.pi.common;

public enum QueryType {
    // 选择器函数
    FIRST(FunctionType.SELECTOR, "first", "Select the first value in the series", -1),
    LAST(FunctionType.SELECTOR, "last", "Select the last value in the series", -1),
    SAMPLE(FunctionType.SELECTOR, "sample", "Select a sample of values from the series", -1),
    PERCENTILE(FunctionType.SELECTOR, "percentile", "Calculate a percentile across the series", -1),
    TOP(FunctionType.SELECTOR, "top", "Select the top N values in the series", 99), // 默认 N 为 99
    BOTTOM(FunctionType.SELECTOR, "bottom", "Select the bottom N values in the series", 99), // 默认 N 为 99

    // 聚合函数
    FILL(FunctionType.AGGREGATION, "fill", "Fill missing values in the series with a specified value or method", -1),
    INTEGRAL(FunctionType.AGGREGATION, "integral", "Calculate the integral of the series over time", -1),
    SPREAD(FunctionType.AGGREGATION, "spread", "Calculate the difference between the maximum and minimum values in the series", -1),
    STDDEV(FunctionType.AGGREGATION, "stddev", "Calculate the standard deviation of the series", -1),
    MEAN(FunctionType.AGGREGATION, "mean", "Calculate the average of the series", -1),
    MEDIAN(FunctionType.AGGREGATION, "median", "Calculate the median of the series", -1),
    MAX(FunctionType.AGGREGATION, "max", "Find the maximum value in the series", -1),
    MIN(FunctionType.AGGREGATION, "min", "Find the minimum value in the series", -1),

    // 转换函数
    DERIVATIVE(FunctionType.TRANSFORMATION, "derivative", "Calculate the rate of change per unit of time for the series", -1),
    DIFFERENCE(FunctionType.TRANSFORMATION, "difference", "Calculate the difference between consecutive points in the series", -1),

    // 预测函数
    HOLT_WINTERS(FunctionType.FORECAST, "holtWinters", "Apply Holt-Winters exponential smoothing to the series", -1);

    // 枚举内部类表示函数类型
    public enum FunctionType {
        SELECTOR,
        AGGREGATION,
        TRANSFORMATION,
        FORECAST
    }

    private final FunctionType functionType;
    private final String functionName;
    private final String description;
    private final int n;

    // 枚举构造器
    QueryType(FunctionType functionType, String functionName, String description, int n) {
        this.functionType = functionType;
        this.functionName = functionName;
        this.description = description;
        this.n = n;
    }

    // 获取函数类型的 getter 方法
    public FunctionType getFunctionType() {
        return functionType;
    }

    // 获取函数名称的 getter 方法
    public String getFunctionName() {
        return functionName;
    }

    // 获取描述的 getter 方法
    public String getDescription() {
        return description;
    }

    // 获取 N 参数的 getter 方法
    public int getN() {
        return n;
    }

    public boolean isTopOrBottom() {
        return this == TOP || this == BOTTOM;
    }
    // 重写 toString 方法，以便于调试和输出
    @Override
    public String toString() {
        return "QueryType{" +
                "functionType=" + functionType +
                ", functionName='" + functionName + '\'' +
                ", description='" + description + '\'' +
                ", n=" + n +
                '}';
    }

}
