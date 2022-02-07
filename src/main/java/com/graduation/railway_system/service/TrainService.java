package com.graduation.railway_system.service;

import com.graduation.railway_system.model.*;

import java.sql.Date;
import java.util.List;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/21 22:10
 */
public interface TrainService {

    void createTrainSchedule(TrainSchedule trainSchedule);

    void createRailway(Long id, List<CreateRailwayRequest.Station> stations);

    Long getNewRailwayId();

    List<TrainScheduleUnitVo> queryTrainScheduleUnit(String startStation, String terminalStation, Date afterTime);

    List<Long> getAllRailway();

    List<RailwayStation> getAllStationsByRailwayId(Long railwayId);

    List<GetTrainScheduleResponse> getAllTrainSchedule();

    int deleteTrainSchedule(Long trainId);
}
