package com.otto.common.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Random;
import java.util.UUID;

/**
 * 字符串 / 编码工具
 */
public class StringTools {

    public static boolean isEmpty(String str) {
        return StringUtils.isEmpty(str) || "null".equals(str) || "\u0000".equals(str);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成指定位数随机数字（用户 ID 等）
     */
    public static String getRandomNumber(Integer count) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成随机字符串（大小写字母 + 数字）
     */
    public static String getRandomString(Integer count) {
        String source = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            sb.append(source.charAt(random.nextInt(source.length())));
        }
        return sb.toString();
    }

    public static String encodeMd5(String source) {
        return isEmpty(source) ? null : DigestUtils.md5Hex(source);
    }

    /**
     * 校验邮箱
     */
    public static boolean isEmail(String email) {
        return isNotEmpty(email) && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
