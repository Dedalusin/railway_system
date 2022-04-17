package com.graduation.railway_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.graduation.railway_system.model.*;
import com.graduation.railway_system.repository.RailwayStationMapper;
import com.graduation.railway_system.repository.TrainScheduleUnitMapper;
import com.graduation.railway_system.repository.TrainScheduleMapper;
import com.graduation.railway_system.service.TrainService;
import com.graduation.railway_system.utils.RedisUtil;
import net.sf.jsqlparser.statement.select.Distinct;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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

    @Autowired
    RedisTemplate redisTemplate;

//    private final LoadingCache<Long, TrainScheduleUnitVo> idToVoCache = CacheBuilder.newBuilder()
//            //设置并发级别为8，并发级别是指可以同时写缓存的线程数
//            .concurrencyLevel(8)
//            //设置写缓存后8s过期
//            .expireAfterWrite(8, TimeUnit.SECONDS)
//            //设置缓存容器的初始容量为10
//            .initialCapacity(10)
//            //设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
//            .maximumSize(100)
//            //设置要统计缓存的命中率
//            .recordStats()
//            //build方法中可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
//            .build(
//                    new CacheLoader<Long, TrainScheduleUnitVo>() {
//                        @Override
//                        public TrainScheduleUnitVo load(Long s) throws Exception {
//                            TrainScheduleUnitVo vo = (TrainScheduleUnitVo) redisTemplate.opsForValue().get(s);
//                            if (vo == null) {
//                                vo =
//                            }
//                            return vo;
//                        }
//
//                        @Override
//                        public Map<Long, TrainScheduleUnitVo> loadAll(Iterable<? extends Long> keys) {
//                            List<TrainScheduleUnitVo> vos = redisTemplate.opsForValue().multiGet((Collection) keys);
//                            return vos == null? null : vos.stream().collect(Collectors.toMap(TrainScheduleUnitVo::getTrainId, e -> e));
//                        }
//                    }
//            );

    private final LoadingCache<String, HashMap<Long, TrainScheduleUnitVo>> searchToIds = CacheBuilder.newBuilder()
            //设置并发级别为8，并发级别是指可以同时写缓存的线程数
            .concurrencyLevel(8)
            //设置写缓存后30分钟过期
            .expireAfterWrite(30, TimeUnit.MINUTES)
            //设置缓存容器的初始容量为10
            .initialCapacity(10)
            //设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
            .maximumSize(100)
            //设置要统计缓存的命中率
            .recordStats()
            //build方法中可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
            .build(
                    new CacheLoader<String, HashMap<Long, TrainScheduleUnitVo>>() {
                        @Override
                        public HashMap<Long, TrainScheduleUnitVo> load(String s) {
                            Map<Long, TrainScheduleUnitVo> map = redisTemplate.opsForHash().entries(s);
                            for (Long key : map.keySet()) {
                                map.get(key).setRemainingSeats((Integer) redisTemplate.opsForHash().get(s+"Seat", key));
                            }
                            return (HashMap<Long, TrainScheduleUnitVo>) map;
                        }
                    }
            );

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
     *
     * @param id       铁路的id
     * @param stations 站点
     */
    @Override
    public void createRailway(Long id, List<CreateRailwayRequest.Station> stations) {
        for (CreateRailwayRequest.Station station : stations) {
            railwayStationMapper.insert(RailwayStation.builder().railwayId(id).num(station.getStationNum()).station(station.getStationName()).build());
        }
    }

    /**
     * @param startStation
     * @param terminalStation
     * @param date
     * @return 第一次查询为全量，后续通过canal增量添加redis
     */
    @Override
    public List<TrainScheduleUnitVo> queryTrainScheduleUnit(String startStation, String terminalStation, Date date) {
        //先查缓存
        String key = startStation + terminalStation + String.format("%tF", date);
        List<TrainScheduleUnitVo> vos = null;
        try {
            vos = Lists.newArrayList(searchToIds.get(key).values());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (vos != null && vos.size() != 0) {
            return vos;
        }
        //未击中缓存，查询数据库
        vos = queryTrainScheduleUnitFromSql(startStation, terminalStation, date);
        //回写缓存
        redisTemplate.opsForHash().putAll(key, vos.stream().collect(Collectors.toMap(e -> e.getTrainId().toString(), Function.identity())));
        redisTemplate.opsForHash().putAll(key+"Seat", vos.stream().collect(Collectors.toMap(e -> e.getTrainId().toString(), TrainScheduleUnitVo::getMaxPeople)));
        return vos;
    }

    @Override
    public void updateTrainScheduleUnit(Long trainId, Long railwayId, String startStation, String terminalStation, int num) {
        //转换station名为序号便于处理
        int startNum = 0;
        int terminalNum = 0;
        List<RailwayStation> stations = railwayStationMapper.selectList(new LambdaQueryWrapper<RailwayStation>().eq(RailwayStation::getRailwayId, railwayId));
        for (RailwayStation station : stations) {
            if (station.getStation().equals(startStation)) {
                startNum = station.getNum();
            } else if (station.getStation().equals(terminalStation)) {
                terminalNum = station.getNum();
            }
        }
        TrainSchedule schedule = trainScheduleMapper.selectById(trainId);
        TrainScheduleUnit root = trainScheduleUnitMapper.selectOne(new LambdaQueryWrapper<TrainScheduleUnit>().eq(TrainScheduleUnit::getTrainId, trainId)
                .eq(TrainScheduleUnit::getStartStation, schedule.getStartStation()).eq(TrainScheduleUnit::getTerminalStation, schedule.getTerminalStation()));
        updateTrainSegmentTree(root, startNum, terminalNum, num);
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
    public GetTrainScheduleResponse getTrainSchedule(Long trainId) {
        List<GetTrainScheduleResponse>  trainList = trainScheduleMapper.getAllTrainSchedule().stream().filter(e -> e.getTrainId().equals(trainId)).collect(Collectors.toList());
        return trainList != null && trainList.size() > 0 ? trainList.get(0) : null;
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

    public List<TrainScheduleUnitVo> queryTrainScheduleUnitFromSql(String startStation, String terminalStation, Date date) {
        //查询包含两站的railway, 并且start < terminal
        List<RailwayStationDTO> railwayStationDTOS = railwayStationMapper.getContainsTwoStationRailwayIds(startStation, terminalStation);
        //根据railway，查询Schedele行车计划
        List<TrainSchedule> trainSchedules = new ArrayList<>();
        for (RailwayStationDTO dto : railwayStationDTOS) {
            trainSchedules.addAll(trainScheduleMapper.selectList(new QueryWrapper<TrainSchedule>()
                    .eq("railway_id", dto.getRailwayId())
                    .ge("start_time", getStartOfDay(date))
                    .le("start_time", getEndOfDay(date))));
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
        //过滤掉搜寻站点比计划线路左右两边长的情况
        treeRoots = treeRoots.stream().filter(e -> {
            String[] unitId = e.getUnitId().split("-");
            if (railwayMap.get(e.getRailwayId()).getStartNum() < Integer.parseInt(unitId[0])
                    || railwayMap.get(e.getRailwayId()).getTerminalNum() > Integer.parseInt(unitId[1])) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
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

    // 获得某天最大时间 2020-02-19 23:59:59
    public static Date getEndOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        ;
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    // 获得某天最小时间 2020-02-17 00:00:00
    public static Date getStartOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 创建线段树
     *
     * @param trainScheduleUnit root
     * @param trainSchedule     主要作用在于提供各个站点的时间
     */
    private void createTrainSegmentTree(TrainScheduleUnit trainScheduleUnit, TrainSchedule trainSchedule) {
        int startStation = trainScheduleUnit.getStartStation();
        int terminalStation = trainScheduleUnit.getTerminalStation();
        int midStation = (startStation + terminalStation) / 2;
        System.out.println(startStation + "-" + midStation + "-" + terminalStation);
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
            TrainScheduleUnit leftSegment = trainScheduleUnitMapper.selectOne(new QueryWrapper<TrainScheduleUnit>().eq("railway_id", root.getRailwayId()).eq("train_id", root.getTrainId()).eq("unit_id", root.getLeftId()));
            return queryTrainSegmentTree(leftSegment, left, right);
        } else if (Integer.parseInt(rchild[0]) <= left && Integer.parseInt(rchild[1]) >= right) {
            //线段树右边
            //TODO 改为redis
            TrainScheduleUnit rightSegment = trainScheduleUnitMapper.selectOne(new QueryWrapper<TrainScheduleUnit>().eq("railway_id", root.getRailwayId()).eq("train_id", root.getTrainId()).eq("unit_id", root.getRightId()));
            return queryTrainSegmentTree(rightSegment, left, right);
        } else {
            //线段树中间
            //TODO 改为redis
            TrainScheduleUnit leftSegment = trainScheduleUnitMapper.selectOne(new QueryWrapper<TrainScheduleUnit>().eq("railway_id", root.getRailwayId()).eq("train_id", root.getTrainId()).eq("unit_id", root.getLeftId()));
            TrainScheduleUnit rightSegment = trainScheduleUnitMapper.selectOne(new QueryWrapper<TrainScheduleUnit>().eq("railway_id", root.getRailwayId()).eq("train_id", root.getTrainId()).eq("unit_id", root.getRightId()));
            TrainScheduleUnit leftResult = queryTrainSegmentTree(leftSegment, left, Integer.parseInt(lchild[1]));
            TrainScheduleUnit rightResult = queryTrainSegmentTree(rightSegment, Integer.parseInt(rchild[0]), right);
            TrainScheduleUnit result = new TrainScheduleUnit();
            BeanUtils.copyProperties(leftResult, result);
            result.setTerminalTime(rightResult.getTerminalTime());
            result.setTerminalStation(rightResult.getTerminalStation());
            result.setRemainingSeats(Math.min(leftResult.getRemainingSeats(), rightResult.getRemainingSeats()));
            result.setPrice(leftResult.getPrice() + rightResult.getPrice());
            return result;
        }
    }

    private void updateTrainSegmentTree(TrainScheduleUnit root, int left, int right, int num) {
        if (root == null) {
            return;
        }

        trainScheduleUnitMapper.update(null, new LambdaUpdateWrapper<TrainScheduleUnit>().eq(TrainScheduleUnit::getTrainId, root.getTrainId())
                .eq(TrainScheduleUnit::getRailwayId, root.getRailwayId()).eq(TrainScheduleUnit::getUnitId, root.getUnitId()).set(TrainScheduleUnit::getRemainingSeats, root.getRemainingSeats() - num));

        if (left + 1 >= right) {
            return;
        }

        String[] lchild = root.getLeftId().split("-");
        String[] rchild = root.getRightId().split("-");


        if (Integer.parseInt(lchild[0]) <= left && Integer.parseInt(lchild[1]) >= right) {
            //线段树左边
            TrainScheduleUnit leftSegment = trainScheduleUnitMapper.selectOne(new QueryWrapper<TrainScheduleUnit>().eq("railway_id", root.getRailwayId()).eq("train_id", root.getTrainId()).eq("unit_id", root.getLeftId()));
            updateTrainSegmentTree(leftSegment, left, right, num);
        } else if (Integer.parseInt(rchild[0]) <= left && Integer.parseInt(rchild[1]) >= right) {
            //线段树右边
            TrainScheduleUnit rightSegment = trainScheduleUnitMapper.selectOne(new QueryWrapper<TrainScheduleUnit>().eq("railway_id", root.getRailwayId()).eq("train_id", root.getTrainId()).eq("unit_id", root.getRightId()));
            updateTrainSegmentTree(rightSegment, left, right, num);
        } else {
            //线段树中间
            TrainScheduleUnit leftSegment = trainScheduleUnitMapper.selectOne(new QueryWrapper<TrainScheduleUnit>().eq("railway_id", root.getRailwayId()).eq("train_id", root.getTrainId()).eq("unit_id", root.getLeftId()));
            TrainScheduleUnit rightSegment = trainScheduleUnitMapper.selectOne(new QueryWrapper<TrainScheduleUnit>().eq("railway_id", root.getRailwayId()).eq("train_id", root.getTrainId()).eq("unit_id", root.getRightId()));
            updateTrainSegmentTree(leftSegment, left, Integer.parseInt(lchild[1]), num);
            updateTrainSegmentTree(rightSegment, Integer.parseInt(rchild[0]), right, num);
        }
    }
}
