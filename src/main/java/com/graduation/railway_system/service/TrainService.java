package com.graduation.railway_system.service;

import com.graduation.railway_system.model.*;

import java.util.Date;
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

    void updateTrainScheduleUnit(Long trainId, Long railwayId, String startStation, String terminalStation, int num);

    List<Long> getAllRailway();

    List<RailwayStation> getAllStationsByRailwayId(Long railwayId);

    List<GetTrainScheduleResponse> getAllTrainSchedule();

    GetTrainScheduleResponse getTrainSchedule(Long trainId);

    int deleteTrainSchedule(Long trainId);

    List<String> getAllStations();

}
