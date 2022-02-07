package com.graduation.railway_system.controller;

import com.graduation.railway_system.model.ResponseVo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/5 21:38
 */
@RestController
@RequestMapping(value = "/order")
public class OrderController {

    @RequestMapping(value = "/createDelayedOrder")
    public ResponseVo createDelayedOrder() {



    }


}
