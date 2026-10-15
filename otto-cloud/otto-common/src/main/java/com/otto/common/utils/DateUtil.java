package com.otto.common.utils;

import com.otto.common.entity.enums.DateTimePatternEnum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String format(Date date, String pattern) {
        return date == null ? null : new SimpleDateFormat(pattern).format(date);
    }

    public static Date parse(String date, String pattern) {
        if (StringTools.isEmpty(date)) {
            return null;
        }
        try {
            return new SimpleDateFormat(pattern).parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String formatNow() {
        return format(new Date(), DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern());
    }

    public static Date getAfterDate(Integer day) {
        return new Date(System.currentTimeMillis() + day * 24L * 60 * 60 * 1000);
    }
}
