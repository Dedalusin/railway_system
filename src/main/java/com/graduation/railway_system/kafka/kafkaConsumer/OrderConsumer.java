package com.graduation.railway_system.kafka.kafkaConsumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.graduation.railway_system.common.DelayQueueInstance;
import com.graduation.railway_system.exception.RepeatPaymentException;
import com.graduation.railway_system.model.DelayOrder;
import com.graduation.railway_system.model.Order;
import com.graduation.railway_system.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/8 16:35
 */
@Component
@Slf4j
public class OrderConsumer {

    @Autowired
    OrderService orderService;

    private final Gson gson = new GsonBuilder().create();
    /**
     * 使用delayqueue作为延迟的中间服务层
     * @param record
     */
    @KafkaListener(topics = {"success_order"})
    public void delayOrderConsume(ConsumerRecord<String, String> record) {
        Order order = gson.fromJson(record.value(), Order.class);
        try {
            orderService.createSucessOrder(order);
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof RepeatPaymentException) {
                log.error("重复购买订单");
            }
        }
    }

    @KafkaListener(topics = {"fail_order"})
    public void failOrderConsume(ConsumerRecord<String, String> record) {
        DelayOrder order = gson.fromJson(record.value(), DelayOrder.class);
        log.info("a failOrder coming: "+ order.toString());
        orderService.createFailOrder(order);
    }
}
