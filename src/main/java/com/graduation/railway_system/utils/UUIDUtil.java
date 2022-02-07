package com.graduation.railway_system.utils;

import java.util.Random;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/25 14:23
 */
public class UUIDUtil {
    /**
     *
     * @return
     * 线程安全的token生成器
     */
    public static synchronized String generateToken() {
        Random random = new Random();
        int num = random.nextInt(900) + 100;
        return System.currentTimeMillis() + String.valueOf(num);
    }
}
