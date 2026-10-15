package com.otto.common.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象拷贝工具
 */
public class CopyTools {

    public static <T> T copy(Object source, Class<T> clazz) {
        if (source == null) {
            return null;
        }
        try {
            T target = clazz.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象拷贝失败:" + clazz.getName(), e);
        }
    }

    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> clazz) {
        List<T> targetList = new ArrayList<>();
        if (sourceList == null) {
            return targetList;
        }
        for (S source : sourceList) {
            targetList.add(copy(source, clazz));
        }
        return targetList;
    }
}
