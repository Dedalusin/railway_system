package com.graduation.railway_system.kafka.kafkaSender;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.graduation.railway_system.exception.KafkaSenderException;
import com.graduation.railway_system.model.DelayOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/7 23:02
 */
@Component
@Slf4j
public class KafkaSender {
    /**
     * used by
     * kafkaTemplate.send("topic", gson.toJson(message));
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaSender(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private Gson gson = new GsonBuilder().create();

    public Future send(String topic, Object o){
        return kafkaTemplate.send(topic, gson.toJson(o));
    }

}
