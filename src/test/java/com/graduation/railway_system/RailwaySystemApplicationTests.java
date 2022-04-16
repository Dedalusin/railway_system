package com.graduation.railway_system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.graduation.railway_system.model.GetTrainScheduleResponse;
import com.graduation.railway_system.model.RailwayStationDTO;
import com.graduation.railway_system.model.TrainScheduleUnitVo;
import com.graduation.railway_system.model.User;
import com.graduation.railway_system.repository.RailwayStationMapper;
import com.graduation.railway_system.repository.TrainScheduleMapper;
import com.graduation.railway_system.repository.UserMapper;
import com.graduation.railway_system.service.TrainService;
import com.graduation.railway_system.utils.RedisUtil;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@MapperScan("com.graduation.railway_system.repository")
@SpringBootTest
class RailwaySystemApplicationTests {

    @Autowired
    UserMapper userRepository;

    @Autowired
    RailwayStationMapper railwayStationMapper;

    @Autowired
    TrainService trainService;

    @Autowired
    TrainScheduleMapper trainScheduleMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedisUtil redisUtil;
    @Test
    void contextLoads() {
//        User user = User.builder().userName("dd")
//                .identityCardName("cc")
//                .accountType("学生")
//                .identityCardType("身份证")
//                .identityCardId("430611200002205534")
//                .password("111111")
//                .email("15345@2222")
//                .phone("111111")
//                .build();
//        userRepository.insert(user);
//        User user1 = userRepository.selectById(1);
//        userRepository.delete(new QueryWrapper<User>().eq("user_name", "dd"));
//        trainService.createRailway(1L, Arrays.asList(new String[]{"aaa", "ccc"}));
//        List<GetTrainScheduleResponse> list = trainService.getAllTrainSchedule();
//        list.forEach(e -> System.out.print(e.toString()));
//        System.out.println(trainService.deleteTrainSchedule(3
        Object s = null;
        String ss = (String)s;
        System.out.println(ss);
    }

}
