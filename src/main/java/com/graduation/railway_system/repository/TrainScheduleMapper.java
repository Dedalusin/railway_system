package com.graduation.railway_system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graduation.railway_system.model.GetTrainScheduleResponse;
import com.graduation.railway_system.model.TrainSchedule;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/21 22:14
 */
@Repository
public interface TrainScheduleMapper extends BaseMapper<TrainSchedule> {
    @Select({"select sc.railway_id, sc.max_people, sc.price, sc.seat_type, sc.start_time, sc.terminal_time, sc.train_id, sc.train_name, st.start_station, st.terminal_station\n" +
            "from train_schedule as sc, (\n" +
            "select a.railway_id as railway_id, a.num as start_num,a.station as start_station,b.num as terminal_num,b.station as terminal_station\n" +
            "from railway_station as a JOIN railway_station as b\n" +
            "on a.railway_id = b.railway_id\n" +
            "where a.station < b.station\n" +
            ") as st\n" +
            "WHERE sc.railway_id = st.railway_id AND sc.start_station = st.start_num AND sc.terminal_station = st.terminal_num"})
    public List<GetTrainScheduleResponse> getAllTrainSchedule();

}
