package com.graduation.railway_system.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/18 15:37
 */
@Slf4j
public class DateUtil {
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static Date convertDateWithoutMillisecond(Date date) {
        try {
            return sdf.parse(sdf.format(date));
        } catch (Exception e) {
            log.error("日期截掉毫秒错误");
        }
        return null;
    }
}
