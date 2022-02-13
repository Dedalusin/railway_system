package com.graduation.railway_system;

import com.graduation.railway_system.common.DelayQueueInstance;
import com.graduation.railway_system.model.DelayOrder;
import com.graduation.railway_system.service.OrderService;
import com.graduation.railway_system.service.impl.OrderServiceImpl;
import com.graduation.railway_system.utils.SpringContextUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author  Dedalusin
 * @version 1.0
 */
@SpringBootApplication
@MapperScan("com.graduation.railway_system.repository")
public class RailwaySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(RailwaySystemApplication.class, args);
        new Thread(() -> {
            try {
                OrderService orderService = SpringContextUtil.getBean(OrderServiceImpl.class);
                while(true) {
                    DelayOrder delayOrder = DelayQueueInstance.INSTANCE.take();
                    orderService.sendFailOrder(delayOrder);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
