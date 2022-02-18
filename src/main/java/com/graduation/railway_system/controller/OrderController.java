package com.graduation.railway_system.controller;

import com.graduation.railway_system.annotation.NeedSession;
import com.graduation.railway_system.kafka.kafkaSender.KafkaSender;
import com.graduation.railway_system.model.CreateDelayedOrderRequest;
import com.graduation.railway_system.model.DelayOrder;
import com.graduation.railway_system.model.Order;
import com.graduation.railway_system.model.ResponseVo;
import com.graduation.railway_system.service.OrderService;
import com.graduation.railway_system.service.TrainService;
import com.sun.xml.internal.bind.v2.TODO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.concurrent.Future;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/5 21:38
 */
@Slf4j
@Api(tags = "订单操作", hidden = false)
@RestController
@RequestMapping(value = "/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    TrainService trainService;

    @NeedSession
    @ApiOperation(value = "创建延迟订单", httpMethod = "POST")
    @RequestMapping(value = "/createDelayedOrder")
    public ResponseVo createDelayedOrder(@RequestBody CreateDelayedOrderRequest request, HttpSession session) {
        Long userid = Long.parseLong(session.getAttribute("userId").toString());
        DelayOrder delayOrder = new DelayOrder();
        BeanUtils.copyProperties(request, delayOrder);
        delayOrder.setUserId(userid);
        trainService.updateTrainScheduleUnit(request.getTrainId(), request.getRailwayId(), request.getStartStation(), request.getTerminalStation(), 1);
        return orderService.sendDelayOrder(delayOrder);
    }

    @NeedSession
    @ApiOperation(value = "订单支付", httpMethod = "POST")
    @RequestMapping(value = "/payOrder")
    public ResponseVo payOrder(@RequestBody CreateDelayedOrderRequest request, HttpSession session) {
        Long userid = Long.parseLong(session.getAttribute("userId").toString());
        Order order = new Order();
        BeanUtils.copyProperties(request, order);
        order.setUserId(userid);
        return orderService.sendPayOrder(order);
    }

}
