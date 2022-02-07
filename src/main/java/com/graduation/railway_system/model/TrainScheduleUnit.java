package com.graduation.railway_system.model;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.sql.Date;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/21 22:48
 * 列车线路的线段树单元
 */
@Component
@Data
public class TrainScheduleUnit {

    private Long railwayId;

    private Long trainId;

    /**
     * unitId 采用起点和终点的编号进行命名 "1-9"
     */
    private String unitId;

    private Integer startStation;

    private Integer terminalStation;

    private Date startTime;

    private Date terminalTime;

    private Integer maxPeople;

    private Integer  remainingSeats;

    /**
     * 左右子树
     */
    private String leftId;

    private String rightId;

    Double price;

}
