package com.graduation.railway_system.kafka.kafkaConsumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.graduation.railway_system.common.DelayQueueInstance;
import com.graduation.railway_system.model.DelayOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/7 22:48
 */
@Slf4j
@Component
public class DelayedOrderConsumer {

    private final Gson gson = new GsonBuilder().create();

    private static DelayQueue<DelayOrder> delayQueue = DelayQueueInstance.INSTANCE;
    /**
     * 使用delayqueue作为延迟的中间服务层
     * @param record
     */
    @KafkaListener(topics = {"delayOrder"})
    public void delayOrderConsume(ConsumerRecord<String, String> record) {
        DelayOrder delayOrder = gson.fromJson(record.value(), DelayOrder.class);
        log.info("a delayOrder coming" + delayOrder.toString());
        delayQueue.put(delayOrder);
    }
}
