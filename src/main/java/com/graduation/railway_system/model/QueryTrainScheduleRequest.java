package com.graduation.railway_system.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.sql.Date;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/29 20:49
 */
@Data
@Component
@ApiModel
public class QueryTrainScheduleRequest {
    @NotNull(message = "起点站不能为空")
    private String startStation;

    @NotNull(message = "终点站不能为空")
    private String terminalStation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "日期不能为空")
    private Date startTime;
}
