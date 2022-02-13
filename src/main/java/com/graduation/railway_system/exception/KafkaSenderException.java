package com.graduation.railway_system.exception;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/8 11:09
 */
public class KafkaSenderException extends Exception {
    public KafkaSenderException(String message) {
        super(message);
    }
}
