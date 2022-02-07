package com.graduation.railway_system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graduation.railway_system.model.RailwayStation;
import com.graduation.railway_system.model.RailwayStationDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/22 21:19
 */
@Repository
public interface RailwayStationMapper extends BaseMapper<RailwayStation> {

    /**
     *
     * @param startStation
     * @param terminalStation
     * @return
     * 获取包含起点和终点的铁路线，并且保证起点和终点是顺序的
     */
//    @Select({"SELECT a.railway_id\n" +
//            "FROM `railway_station` AS a LEFT JOIN `railway_station` AS b on a.railway_id = b.railway_id\n" +
//            "Where a.station = #{startStation} AND b.station = #{terminalStation} AND a.num < b.num"})
//    public ArrayList<Long> getContainsTwoStationRailwayIds(@Param("startStation") String startStation, @Param("terminalStation") String terminalStation);
    @Select({"SELECT a.railway_id as railway_id, a.num as start_num, a.station as start_station, b.num as terminal_num, b.station as terminal_station\n" +
            "FROM `railway_station` AS a LEFT JOIN `railway_station` AS b on a.railway_id = b.railway_id\n" +
            "Where a.station = #{startStation} AND b.station = #{terminalStation} AND a.num < b.num"})
    public ArrayList<RailwayStationDTO> getContainsTwoStationRailwayIds(@Param("startStation") String startStation, @Param("terminalStation") String terminalStation);

}
