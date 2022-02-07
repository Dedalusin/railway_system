package com.graduation.railway_system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/22 21:10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class RailwayStation implements Serializable {

    private Long railwayId;

    private String station;

    private Integer num;

}
