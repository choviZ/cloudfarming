package com.vv.cloudfarming.common.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 格式转换工具类
 */
public class FormatConvertUtil {

    public static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        if (dateToConvert == null) {
            return null;
        }
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
