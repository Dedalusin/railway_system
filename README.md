### 一个铁路订票系统
- 满足基本的查询、订票需求以及管理员的铁路创建等操作。
- 数据库 
  - 采用线段树结构存放铁路票务数据。
  - 通过binlog以及中间件完成数据库回写缓存维持一致性
- 使用到的技术框架、中间件等：
  springboot
  guava
  redis
  mybatis-plus
  kafka
  swagger
  canal

