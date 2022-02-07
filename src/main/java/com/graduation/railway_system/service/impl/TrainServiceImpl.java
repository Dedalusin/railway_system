package com.graduation.railway_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.graduation.railway_system.model.*;
import com.graduation.railway_system.repository.RailwayStationMapper;
import com.graduation.railway_system.repository.TrainScheduleUnitMapper;
import com.graduation.railway_system.repository.TrainScheduleMapper;
import com.graduation.railway_system.service.TrainService;
import net.sf.jsqlparser.statement.select.Distinct;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/21 22:13
 */
@Service
public class TrainServiceImpl implements TrainService {

    @Autowired
    TrainScheduleMapper trainScheduleMapper;

    @Autowired
    TrainScheduleUnitMapper trainScheduleUnitMapper;

    @Autowired
    RailwayStationMapper railwayStationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTrainSchedule(TrainSchedule trainSchedule) {
        //首先插入trainSchedule表
        trainScheduleMapper.insert(trainSchedule);
        //从trainSchedule中获取自增的trainId
        Long trainId = trainScheduleMapper.selectOne(new QueryWrapper<TrainSchedule>(trainSchedule)).getTrainId();
        trainSchedule.setTrainId(trainId);
        TrainScheduleUnit trainScheduleUnit = new TrainScheduleUnit();
        BeanUtils.copyProperties(trainSchedule, trainScheduleUnit);
        trainScheduleUnit.setRemainingSeats(trainScheduleUnit.getMaxPeople());
        trainScheduleUnit.setUnitId(trainScheduleUnit.getStartStation() + "-" + trainSchedule.getTerminalStation());
        createTrainSegmentTree(trainScheduleUnit, trainSchedule);
    }

    /**
     * 创建铁路线，静态的线路，即后续的火车运行线路都基于此创建，相当于一个父集
     * @param id       铁路的id
     * @param stations 站点
     *
     */
    @Override
    public void createRailway(Long id, List<CreateRailwayRequest.Station> stations) {
        for (CreateRailwayRequest.Station station : stations) {
            railwayStationMapper.insert(RailwayStation.builder().railwayId(id).num(station.getStationNum()).station(station.getStationName()).build());
        }
    }

    @Override
    public List<TrainScheduleUnitVo> queryTrainScheduleUnit(String startStation, String terminalStation, Date afterTime) {
        //查询包含两站的railway, 并且start < terminal
        List<RailwayStationDTO> railwayStationDTOS = railwayStationMapper.getContainsTwoStationRailwayIds(startStation, terminalStation);
        //根据railway，查询Schedele行车计划
        List<TrainSchedule> trainSchedules = new ArrayList<>();
        for (RailwayStationDTO dto : railwayStationDTOS) {
            trainSchedules.addAll(trainScheduleMapper.selectList(new QueryWrapper<TrainSchedule>().eq("railway_id", dto.getRailwayId()).ge("terminal_time", afterTime)));
        }
        //由schedule查询所有的符合条件的线段树root
        List<QueryWrapper<TrainScheduleUnit>> treeRootsWrappers = trainSchedules.stream().map(e -> {
            TrainScheduleUnit unit = new TrainScheduleUnit();
            BeanUtils.copyProperties(e, unit);
            return new QueryWrapper<>(unit);
        }).collect(Collectors.toList());
        List<TrainScheduleUnit> treeRoots = new ArrayList<>();
        for (QueryWrapper wrapper : treeRootsWrappers) {
            treeRoots.addAll(trainScheduleUnitMapper.selectList(wrapper));
        }
        //对dto建立map，railwayId -> dto(包含String的站点在线段树中对应的数字)，便于在后续查询线段树
        Map<Long, RailwayStationDTO> railwayMap = railwayStationDTOS.stream().collect(Collectors.toMap(RailwayStationDTO::getRailwayId, e -> e));
        //查询线段树,并将结果收集为Vo
        List<TrainScheduleUnitVo> vos = new ArrayList<>();
        TrainScheduleUnitVo vo;
        for (TrainScheduleUnit root : treeRoots) {
            TrainScheduleUnit unit = queryTrainSegmentTree(root, railwayMap.get(root.getRailwayId()).getStartNum(), railwayMap.get(root.getRailwayId()).getTerminalNum());
            vo = new TrainScheduleUnitVo();
            BeanUtils.copyProperties(unit, vo);
            vo.setStartStation(startStation);
            vo.setTerminalStation(terminalStation);
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public Long getNewRailwayId() {
        //TODO 思考下railwayId是数据库生成还是前端制定
        return null;
    }

    @Override
    public List<Long> getAllRailway() {
        QueryWrapper<RailwayStation> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("Distinct railway_id");
        return railwayStationMapper.selectList(queryWrapper).stream().map(e -> e.getRailwayId()).collect(Collectors.toList());
    }

    @Override
    public List<RailwayStation> getAllStationsByRailwayId(Long railwayId) {
        return railwayStationMapper.selectList(new LambdaQueryWrapper<RailwayStation>().eq(RailwayStation::getRailwayId, railwayId));
    }

    @Override
    public List<GetTrainScheduleResponse> getAllTrainSchedule() {
        return trainScheduleMapper.getAllTrainSchedule();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTrainSchedule(Long trainId) {
        if (trainScheduleMapper.deleteById(trainId) <= 0) {
            return -1;
        } else {
            trainScheduleUnitMapper.delete(new LambdaQueryWrapper<TrainScheduleUnit>().eq(TrainScheduleUnit::getTrainId, trainId));
        }
        return 1;
    }

    /**
     * 创建线段树
     * @param trainScheduleUnit root
     * @param trainSchedule 主要作用在于提供各个站点的时间
     */
    private void createTrainSegmentTree(TrainScheduleUnit trainScheduleUnit, TrainSchedule trainSchedule) {
        int startStation = trainScheduleUnit.getStartStation();
        int terminalStation = trainScheduleUnit.getTerminalStation();
        int midStation = (startStation + terminalStation) / 2;
        System.out.println(startStation+"-"+midStation+"-"+terminalStation);
        if (startStation + 1 == terminalStation) {
            trainScheduleUnitMapper.insert(trainScheduleUnit);
            return;
        }
        if (startStation + 1 <= midStation) {
            TrainScheduleUnit left = new TrainScheduleUnit();
            BeanUtils.copyProperties(trainScheduleUnit, left);
            left.setUnitId(startStation + "-" + midStation);
            left.setTerminalStation(midStation);
            left.setTerminalTime(trainSchedule.getAllTerminalTime().get(midStation));
            //TODO 价格合理优化
            left.setPrice((midStation - startStation) * trainScheduleUnit.getPrice() / (terminalStation - startStation));
            trainScheduleUnit.setLeftId(left.getUnitId());
            //向下继续填充left的左右子树的id
            createTrainSegmentTree(left, trainSchedule);
        }
        if (midStation + 1 <= terminalStation) {
            TrainScheduleUnit right = new TrainScheduleUnit();
            BeanUtils.copyProperties(trainScheduleUnit, right);
            right.setLeftId(null);
            right.setUnitId(midStation + "-" + terminalStation);
            right.setStartStation(midStation);
            right.setStartTime(trainSchedule.getAllStartTime().get(midStation));
            //TODO 价格合理优化
            right.setPrice((terminalStation - midStation) * trainScheduleUnit.getPrice() / (terminalStation - startStation));
            trainScheduleUnit.setRightId(right.getUnitId());
            //向下继续填充right的左右子树的id
            createTrainSegmentTree(right, trainSchedule);
        }
        //插入数据库
        trainScheduleUnitMapper.insert(trainScheduleUnit);
    }

    private TrainScheduleUnit queryTrainSegmentTree(TrainScheduleUnit root, int left, int right) {
        if (root.getUnitId().equals(left + "-" + right)) {
            return root;
        }

        String[] lchild = root.getLeftId().split("-");
        String[] rchild = root.getRightId().split("-");


        if (Integer.parseInt(lchild[0]) <= left && Integer.parseInt(lchild[1]) >= right) {
            //线段树左边
            //TODO 改为redis
            TrainScheduleUnit leftSegment = trainScheduleUnitMapper.selectOne(new QueryWrapper<TrainScheduleUnit>().eq("railway_id", root.getRailwayId()).eq("train_id", root.getTrainId()).eq("unit_id",root.getLeftId()));
            return queryTrainSegmentTree(leftSegment, left, right);
        } else if (Integer.parseInt(rchild[0]) <= left && Integer.parseInt(rchild[1]) >= right) {
            //线段树右边
            //TODO 改为redis
            TrainScheduleUnit rightSegment = trainScheduleUnitMapper.selectOne(new QueryWrapper<TrainScheduleUnit>().eq("railway_id", root.getRailwayId()).eq("train_id", root.getTrainId()).eq("unit_id",root.getRightId()));
            return queryTrainSegmentTree(rightSegment, left, right);
        } else {
            //线段树中间
            //TODO 改为redis
            TrainScheduleUnit leftSegment = trainScheduleUnitMapper.selectOne(new QueryWrapper<TrainScheduleUnit>().eq("railway_id", root.getRailwayId()).eq("train_id", root.getTrainId()).eq("unit_id",root.getLeftId()));
            TrainScheduleUnit rightSegment = trainScheduleUnitMapper.selectOne(new QueryWrapper<TrainScheduleUnit>().eq("railway_id", root.getRailwayId()).eq("train_id", root.getTrainId()).eq("unit_id",root.getRightId()));
            TrainScheduleUnit leftResult = queryTrainSegmentTree(leftSegment, left, Integer.parseInt(lchild[1]));
            TrainScheduleUnit rightResult = queryTrainSegmentTree(rightSegment, Integer.parseInt(rchild[0]), right);
            TrainScheduleUnit result = new TrainScheduleUnit();
            BeanUtils.copyProperties(leftResult, result);
            result.setTerminalTime(rightResult.getTerminalTime());
            result.setTerminalStation(rightResult.getTerminalStation());
            result.setRemainingSeats(Math.min(leftResult.getRemainingSeats(), rightResult.getRemainingSeats()));
            result.setPrice(leftResult.getPrice()+rightResult.getPrice());
            return result;
        }
    }
}
