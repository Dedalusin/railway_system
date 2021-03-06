package com.graduation.railway_system.service;

import com.graduation.railway_system.exception.RepeatPaymentException;
import com.graduation.railway_system.model.*;

import java.util.List;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/5 21:47
 */
public interface OrderService {

    ResponseVo createDelayedOrder(DelayOrder delayOrder);

    ResponseVo sendDelayOrder(DelayOrder delayOrder);

    ResponseVo sendPayOrder(PayOrder order);

    void sendFailOrder(DelayOrder delayOrder);

    void createFailOrder(DelayOrder order);

    void createSucessOrder(Order order) throws RepeatPaymentException;

    List<Order> getAllOrders(Long userId);

    boolean revertOrder(Long order);

}
