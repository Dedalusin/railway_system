package com.graduation.railway_system.utils;

import com.google.common.cache.*;
import com.graduation.railway_system.model.TrainScheduleUnitVo;
import com.graduation.railway_system.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/4/10 20:07
 */
public class MultiDataSourceUtil {

    @Autowired
    TrainService trainService;

    public static final LoadingCache<String, TrainScheduleUnitVo> loadingCache = CacheBuilder.newBuilder()
            //设置并发级别为8，并发级别是指可以同时写缓存的线程数
            .concurrencyLevel(8)
            //设置写缓存后8秒钟过期
            .expireAfterWrite(8, TimeUnit.SECONDS)
            //设置缓存容器的初始容量为10
            .initialCapacity(10)
            //设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
            .maximumSize(100)
            //设置要统计缓存的命中率
            .recordStats()
            //build方法中可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
            .build(
                    new CacheLoader<String, TrainScheduleUnitVo>() {
                        @Override
                        public TrainScheduleUnitVo load(String s) throws Exception {
                            //redis 中的车次采用json存储，该json可以直接返回给前端，前端直接取即可，避免后端反复序列化
                            return null;
                        }
                    }
            );
}
