package com.graduation.railway_system.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.stereotype.Component;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/24 13:19
 */
@Component
@Data
@ToString
public class RailwayStationDTO {
    private Long railwayId;

    private Integer startNum;

    private Integer terminalNum;

    private String startStation;

    private String terminalStation;
}
