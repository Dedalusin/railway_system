package com.graduation.railway_system.exception;

import org.apache.kafka.common.protocol.types.Field;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/8 17:09
 */
public class RepeatPaymentException extends Exception{
    public RepeatPaymentException(String message) {
        super(message);
    }
}
