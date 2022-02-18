package com.graduation.railway_system.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/5 21:41
 */
@Data
@Component
@ApiModel
public class CreateDelayedOrderRequest {
    @NotNull(message = "起点不能为空")
    private String startStation;

    @NotNull(message = "终点不能为空")
    private String terminalStation;

//    @NotNull(message = "时间不能为空")
//    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
//    private Date orderTime;

    @NotNull(message = "列车id不能为空")
    private Long trainId;

    @NotNull(message = "线路id不能为空")
    private Long railwayId;

    @NotNull(message = "价格不能为空")
    private Double price;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date terminalTime;
}
