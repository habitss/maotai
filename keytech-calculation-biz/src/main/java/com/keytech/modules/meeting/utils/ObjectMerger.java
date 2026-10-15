package com.keytech.modules.meeting.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.lang.reflect.Field;

public class ObjectMerger {

    private static final Set<Class<?>> NUMERIC_TYPES = new HashSet<>(Arrays.asList(
            byte.class, short.class, int.class, long.class, float.class, double.class,
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class
    ));

    public static void mergeNonNullProperties(Object source, Object target) throws IllegalAccessException {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target objects must not be null");
        }

        Class<?> clazz = source.getClass();
        if (!clazz.equals(target.getClass())) {
            throw new IllegalArgumentException("Source and target objects must be of the same type");
        }

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (isNumericField(field)) {
                Object sourceValue = field.get(source);
                if (isNonZeroValue(sourceValue)) {
                    field.set(target, sourceValue);
                }
            }
        }
    }

    private static boolean isNumericField(Field field) {
        return NUMERIC_TYPES.contains(field.getType());
    }

    private static boolean isNonZeroValue(Object value) {
        if (value == null) {
            return false;
        }
        Class<?> type = value.getClass();

        if (type == Byte.class || type == byte.class) {
            return (Byte) value != 0;
        } else if (type == Short.class || type == short.class) {
            return (Short) value != 0;
        } else if (type == Integer.class || type == int.class) {
            return (Integer) value != 0;
        } else if (type == Long.class || type == long.class) {
            return (Long) value != 0;
        } else if (type == Float.class || type == float.class) {
            return Float.compare((Float) value, 0.0f) != 0;
        } else if (type == Double.class || type == double.class) {
            return Double.compare((Double) value, 0.0) != 0;
        }
        return false;
    }

}
