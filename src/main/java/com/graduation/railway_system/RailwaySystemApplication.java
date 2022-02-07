package com.graduation.railway_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author  Dedalusin
 * @version 1.0
 */
@SpringBootApplication
@MapperScan("com.graduation.railway_system.repository")
public class RailwaySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(RailwaySystemApplication.class, args);
    }

}
