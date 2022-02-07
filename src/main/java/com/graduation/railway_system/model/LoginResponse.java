package com.graduation.railway_system.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/24 22:23
 */
@Data
@Component
public class LoginResponse implements Serializable {

    private String userName;

    private Long userId;

}
