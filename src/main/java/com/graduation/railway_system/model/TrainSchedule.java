package com.graduation.railway_system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/21 22:15
 */
@Data
@Component
public class TrainSchedule {
    @TableId(type = IdType.AUTO)
    private Long trainId;

    private Long railwayId;

    private String trainName;

    private Integer startStation;

    private Integer terminalStation;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date terminalTime;

    private Integer maxPeople;

    /**
     * 0 为硬座 1 为卧铺
     */
    private Integer seatType;

    /**
     * 按照station的编号顺序
     */
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    List<Date> allStartTime;

    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    List<Date> allTerminalTime;

    Double price;

}
