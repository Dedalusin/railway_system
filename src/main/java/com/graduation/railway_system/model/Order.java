package com.graduation.railway_system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 订单状态：isPay-isDelay
 * 0-0: 过期失效订单 1:0 已支付订单 0:1 未支付但正在过期时间内
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/8 16:36
 */
@Data
@NoArgsConstructor
@Component
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long orderId;

    private Long userId;

    private Long trainId;

    private String startStation;

    private String terminalStation;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date terminalTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Double price;

    /**
     * 是否完成支付
     */
    private int isPay;
    /**
     * 是否正在处于延迟状态中
     */
    private int isDelay;
}
