package com.graduation.railway_system.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/29 16:03
 */
@Data
@Component
public class CreateRailwayRequest {
    @NotNull(message = "railwayId不能为空")
    private Long railwayId;

    List<Station> stationList;

    @Data
    public static class Station {

        private int stationNum;

        private String stationName;
    }

}
