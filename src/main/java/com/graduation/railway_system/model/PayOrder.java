package com.graduation.railway_system.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/4/9 20:27
 */
@Data
public class PayOrder {
    @NotNull(message = "订单号不能为空")
    private Long orderId;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

}
