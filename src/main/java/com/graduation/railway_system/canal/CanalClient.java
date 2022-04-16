package com.graduation.railway_system.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.alibaba.otter.canal.protocol.Message;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.graduation.railway_system.model.RailwayStation;
import com.graduation.railway_system.model.TrainScheduleUnitVo;
import com.graduation.railway_system.repository.RailwayStationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/4/16 17:42
 */
@Slf4j
@Component
public class CanalClient implements InitializingBean {

    private final static int BATCH_SIZE = 1000;

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RailwayStationMapper railwayStationMapper;

    //存放station和具体名称的对应关系
//    private final LoadingCache<String, String> searchToIds = CacheBuilder.newBuilder()
//            //设置并发级别为8，并发级别是指可以同时写缓存的线程数
//            .concurrencyLevel(8)
//            //设置写缓存后30分钟过期
//            .expireAfterWrite(30, TimeUnit.MINUTES)
//            //设置缓存容器的初始容量为10
//            .initialCapacity(10)
//            //设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
//            .maximumSize(100)
//            //设置要统计缓存的命中率
//            .recordStats()
//            //build方法中可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
//            .build(
//                    new CacheLoader<String, String>() {
//                        @Override
//                        public String load(String s) {
//                            //key为 railwayId-stationNum value : stationName
//                            long railway = Long.parseLong(s.split("-")[0]);
//                            int stationNum = Integer.parseInt(s.split("-")[1]);
//                            return Optional
//                                    .ofNullable((String) redisTemplate.opsForValue().get(s))
//                                    .orElse(railwayStationMapper.selectOne(new LambdaQueryWrapper<RailwayStation>()
//                                            .eq(RailwayStation::getRailwayId, railway)
//                                            .eq(RailwayStation::getNum, stationNum)).getStation());
//                        }
//                    }
//            );

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            // 创建链接
            CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("127.0.0.1", 11111), "example", "", "");
            try {
                //打开连接
                connector.connect();
                //订阅数据库表,全部表
                connector.subscribe(".*\\.order");
                //回滚到未进行ack的地方，下次fetch的时候，可以从最后一个没有ack的地方开始拿
                connector.rollback();
                while (true) {
                    // 获取指定数量的数据
                    Message message = connector.getWithoutAck(BATCH_SIZE);
                    //获取批量ID
                    long batchId = message.getId();
                    //获取批量的数量
                    int size = message.getEntries().size();
                    //如果没有数据
                    if (batchId == -1 || size == 0) {
                        try {
                            //线程休眠2秒
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //如果有数据,处理数据
                        printEntry(message.getEntries());
                    }
                    //进行 batch id 的确认。确认之后，小于等于此 batchId 的 Message 都会被确认。
                    connector.ack(batchId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connector.disconnect();
            }
        }).start();
    }

    /**
     * 打印canal server解析binlog获得的实体类信息
     * 考虑到，数据库层面使用线段树进行记录，不适合用来回调更新redis，采用order流水记录更新
     */
    private void printEntry(List<Entry> entrys) {
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                //开启/关闭事务的实体类型，跳过
                continue;
            }
            //RowChange对象，包含了一行数据变化的所有特征
            //比如isDdl 是否是ddl变更操作 sql 具体的ddl sql beforeColumns afterColumns 变更前后的数据字段等等
            RowChange rowChage;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e);
            }
            //获取操作类型：insert/update/delete类型
            EventType eventType = rowChage.getEventType();
            //打印Header信息
            System.out.println(String.format("================》; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));
            //判断是否是DDL语句
            if (rowChage.getIsDdl()) {
                System.out.println("================》;isDdl: true,sql:" + rowChage.getSql());
            }
            //获取RowChange对象里的每一行数据，打印出来
            for (RowData rowData : rowChage.getRowDatasList()) {
                //如果是删除语句
                if (eventType == EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList());

                } else if (eventType == EventType.INSERT) {
                    //如果是新增语句，订单生成，更新redis座位表
                    printColumn(rowData.getAfterColumnsList());
                    StringBuilder key = new StringBuilder();
                    String id = "";
                    for (Column column : rowData.getAfterColumnsList()) {
                        switch (column.getName()) {
                            case "start_station":
                            case "terminal_station":
                                key.append(column.getValue());
                                break;
                            case "start_time":
                                key.append(column.getValue().split(" ")[0]);
                                break;
                            case "train_id":
                                id = column.getValue();
                                break;
                            default:
                        }
                    }
                    key.append("Seat");
                    redisTemplate.opsForHash().increment(key.toString(), id, -1);
                } else {
                    boolean needChange = true;
                    StringBuilder key = new StringBuilder();
                    String id = "";
                    List<Column> preCol = rowData.getBeforeColumnsList();
                    List<Column> aftCol = rowData.getAfterColumnsList();
                    if (!preCol.get(0).getName().equals(aftCol.get(0).getName())) {
                        log.error("更改前后列名不符");
                    }
                    for (int i = 0; i < preCol.size(); i++) {
                        if (preCol.get(i).getName().equals("is_pay") && aftCol.get(i).getName().equals("is_pay")) {
                            if (!preCol.get(i).getValue().equals(aftCol.get(i).getValue())) {
                                needChange = false;
                                return;
                            }
                        } else if (preCol.get(i).getName().equals("is_delay") && aftCol.get(i).getName().equals("is_delay")) {
                            if (preCol.get(i).getValue().equals(aftCol.get(i).getValue())) {
                                needChange = false;
                                return;
                            }
                        }
                        switch (preCol.get(i).getName()) {
                            case "start_station":
                            case "terminal_station":
                                key.append(preCol.get(i).getValue());
                                break;
                            case "start_time":
                                key.append(preCol.get(i).getValue().split(" ")[0]);
                                break;
                            case "train_id":
                                id = preCol.get(i).getValue();
                                break;
                            default:
                        }
                    }
                    if (needChange) {
                        key.append("Seat");
                        redisTemplate.opsForHash().increment(key.toString(), id, 1);
                    }
                }
            }
        }
    }

    private static void printColumn(List<Column> columns) {
        for (Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}
