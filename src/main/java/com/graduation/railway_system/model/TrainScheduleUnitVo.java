package com.graduation.railway_system.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/24 13:40
 */
@Data
@Component
@ApiModel
public class TrainScheduleUnitVo implements Serializable {

    private Long railwayId;

    private Long trainId;

    private String startStation;

    private String terminalStation;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date terminalTime;

    private Integer maxPeople;

    private Integer  remainingSeats;

    Double price;

}
