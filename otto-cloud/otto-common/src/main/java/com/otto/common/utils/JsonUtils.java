package com.otto.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import java.util.List;

/**
 * JSON 工具（fastjson2）
 */
public class JsonUtils {

    public static String toJsonString(Object obj) {
        return obj == null ? null : JSON.toJSONString(obj);
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        return StringTools.isEmpty(json) ? null : JSON.parseObject(json, clazz);
    }

    public static <T> List<T> toList(String json, Class<T> clazz) {
        return StringTools.isEmpty(json) ? List.of() : JSON.parseArray(json, clazz);
    }

    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        return StringTools.isEmpty(json) ? null : JSON.parseObject(json, typeReference);
    }
}
