package com.graduation.railway_system.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.graduation.railway_system.model.*;
import com.graduation.railway_system.service.TrainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/28 15:02
 */
@Api(tags = "铁路信息操作", hidden = false)
@RestController
@RequestMapping("/train")
public class TrainController {

    @Autowired
    TrainService trainService;

    /**
     * request的stationsList中的num需要从0开始
     * @param request
     */
    @ApiOperation(value = "创建铁路线", httpMethod = "POST")
    @RequestMapping(value = "/createRailway", method = RequestMethod.POST)
    public ResponseVo createRailway(@RequestBody CreateRailwayRequest request) {
        trainService.createRailway(request.getRailwayId(), request.getStationList());
        return ResponseVo.success("创建成功");
    }

    @ApiOperation(value = "创建列车计划", httpMethod = "POST")
    @RequestMapping(value = "/createTrainSchedule", method = RequestMethod.POST)
    public void createSchedule(@RequestBody TrainSchedule schedule) {
        trainService.createTrainSchedule(schedule);
    }

    @ApiOperation(value = "查询列车", httpMethod = "POST")
    @RequestMapping(value = "/queryTrain", method = RequestMethod.POST)
    public ResponseVo queryTrain(@RequestBody QueryTrainScheduleRequest request) {
         List<TrainScheduleUnitVo> vos = trainService.queryTrainScheduleUnit(request.getStartStation(),request.getTerminalStation(),request.getStartTime());
         return ResponseVo.success(vos);
    }

    @ApiOperation(value = "查询所有铁路线id", httpMethod = "POST")
    @RequestMapping(value = "/getAllRailwayId", method = RequestMethod.GET)
    public ResponseVo getAllRailwayId() {
        List<Long> railwayIds = trainService.getAllRailway();
        return ResponseVo.success(railwayIds);
    }

    @ApiOperation(value = "查询所有站点by线路id", httpMethod = "POST")
    @RequestMapping(value = "/getAllStationsByRailwayId", method = RequestMethod.GET)
    public ResponseVo getAllStations(Long railwayId) {
        return ResponseVo.success(trainService.getAllStationsByRailwayId(railwayId));
    }

    @ApiOperation(value = "查询所有列车计划", httpMethod = "POST")
    @RequestMapping(value = "/getAllTrainSchedule", method = RequestMethod.GET)
    public ResponseVo getAllSchedule() {
        return ResponseVo.success(trainService.getAllTrainSchedule());
    }

    @ApiOperation(value = "查询列车计划", httpMethod = "GET")
    @RequestMapping(value = "/getTrainSchedule", method = RequestMethod.GET)
    public ResponseVo getSchedule(@RequestParam Long trainId) {
        GetTrainScheduleResponse responseData = trainService.getTrainSchedule(trainId);
        return responseData == null ? ResponseVo.failed("没有该列车") : ResponseVo.success(responseData);
    }

    @ApiOperation(value = "删除列车计划", httpMethod = "DELETE")
    @RequestMapping(value = "/deleteTrainSchedule", method = RequestMethod.DELETE)
    public ResponseVo deleteTrainSchedule(Long trainId) {
        if (trainService.deleteTrainSchedule(trainId) > 0) {
            return ResponseVo.success("删除成功");
        } else {
            return ResponseVo.failed("删除失败");
        }
    }
    @ApiOperation(value = "查询所有站点", httpMethod = "GET")
    @RequestMapping(value = "/getAllStations", method = RequestMethod.GET)
    public ResponseVo getAllStations() {
        return ResponseVo.success(trainService.getAllStations());
    }

}
