package com.graduation.railway_system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/2/3 19:03
 */
@Component
@Data
@ApiModel
public class GetTrainScheduleResponse implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long trainId;

    private Long railwayId;

    private String trainName;

    private String startStation;

    private String terminalStation;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date terminalTime;

    private Integer maxPeople;

    /**
     * 0 为硬座 1 为卧铺
     */
    private Integer seatType;

}
