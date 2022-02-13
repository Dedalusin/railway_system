package com.graduation.railway_system.common;

import com.graduation.railway_system.model.DelayOrder;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/8 16:20
 */
@Component
public class DelayQueueInstance {
    public static final DelayQueue<DelayOrder> INSTANCE = new DelayQueue<>();
}
