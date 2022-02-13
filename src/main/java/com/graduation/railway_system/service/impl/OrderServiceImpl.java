package com.graduation.railway_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.graduation.railway_system.exception.RepeatPaymentException;
import com.graduation.railway_system.kafka.kafkaSender.KafkaSender;
import com.graduation.railway_system.model.CreateDelayedOrderRequest;
import com.graduation.railway_system.model.DelayOrder;
import com.graduation.railway_system.model.Order;
import com.graduation.railway_system.model.ResponseVo;
import com.graduation.railway_system.repository.OrderMapper;
import com.graduation.railway_system.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.Future;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/5 21:48
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    KafkaSender kafkaSender;

    @Autowired
    OrderMapper orderMapper;

    @Override
    public ResponseVo sendDelayOrder(DelayOrder delayOrder) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date createTime = new Date(System.currentTimeMillis());
        delayOrder.setCreateTime(createTime);
        Order order = new Order();
        BeanUtils.copyProperties(delayOrder, order);
        order.setIsDelay(1);
        order.setIsPay(0);
        orderMapper.insert(order);
        delayOrder.setRunAt(createTime.getTime() + 10000L);
        Future future = kafkaSender.send("delayOrder", delayOrder);
        try {
            future.get();
            return ResponseVo.success("创建订单成功");
        } catch (Exception e) {
            log.error("创建订单失败");
            e.printStackTrace();
            return ResponseVo.failed("创建订单失败");
        }
    }

    @Override
    public ResponseVo sendPayOrder(Order order) {
        Future future = kafkaSender.send("success_order", order);
        try {
            future.get();
            return ResponseVo.success("支付成功");
        } catch (Exception e) {
            log.error("支付失败");
            e.printStackTrace();
            return ResponseVo.failed("支付失败");
        }
    }

    @Override
    public void sendFailOrder(DelayOrder delayOrder) {
        kafkaSender.send("fail_order", delayOrder);
    }

    @Override
    public ResponseVo createDelayedOrder(DelayOrder delayOrder) {
        Date createTime = new Date(System.currentTimeMillis());
        delayOrder.setCreateTime(createTime);
        Order order = new Order();
        BeanUtils.copyProperties(delayOrder, order);
        order.setIsDelay(1);
        order.setIsPay(0);
        orderMapper.insert(order);
        Future future = kafkaSender.send("delayOrder", delayOrder);
        try {
            future.get();
            return ResponseVo.success("创建订单成功");
        } catch (Exception e) {
            log.error("创建订单失败");
            e.printStackTrace();
            return ResponseVo.failed("创建订单失败");
        }
    }


    @Override
    public void createFailOrder(DelayOrder delayOrder) {
        Order existOrder = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getCreateTime, delayOrder.getCreateTime())
                .eq(Order::getUserId, delayOrder.getUserId())
        );
        if (existOrder == null || existOrder.getIsDelay() == 0) {
            //delay为0 => 已经失效或者已经确认购买
            return;
        }
        existOrder.setIsDelay(0);
        orderMapper.updateById(existOrder);
    }

    @Override
    public void createSucessOrder(Order order) throws RepeatPaymentException {
        Order existOrder = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getCreateTime, order.getCreateTime())
                .eq(Order::getUserId, order.getUserId())
        );
        if (existOrder == null) {
            return;
        }
        if (existOrder.getIsPay() == 1) {
            throw new RepeatPaymentException("重复购买订单");
        }
        existOrder.setIsPay(1);
        existOrder.setIsDelay(0);
        orderMapper.updateById(existOrder);
    }
}
