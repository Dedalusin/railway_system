package com.graduation.railway_system.service;

import com.graduation.railway_system.model.CreateDelayedOrderRequest;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/5 21:47
 */
public interface OrderService {

    boolean createDelayedOrder(CreateDelayedOrderRequest request);

}
